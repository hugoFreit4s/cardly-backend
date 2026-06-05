package com.cardly.service;

import com.cardly.domain.User;
import com.cardly.domain.UserRoleENUM;
import com.cardly.repository.UserRepository;
import com.cardly.web.dto.AuthConfigResponse;
import com.cardly.web.dto.AuthResponse;
import com.cardly.web.dto.LoginRequest;
import com.cardly.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PublicIdService publicIdService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@InjectMocks
	private AuthService authService;

	@Test
	void registerCreatesUserWhenEmailFree() {
		when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("password12")).thenReturn("hash");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User u = invocation.getArgument(0);
			u.setId(10);
			return u;
		});
		Instant exp = Instant.now().plus(1, ChronoUnit.HOURS);
		when(jwtService.buildAuthResponse(any(User.class))).thenReturn(new AuthResponse("tok", exp));

		AuthResponse response = authService.register(new RegisterRequest("Name", "  A@B.com  ", "password12"));

		assertThat(response.token()).isEqualTo("tok");
		assertThat(response.expiresAt()).isEqualTo(exp);
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getEmail()).isEqualTo("a@b.com");
		assertThat(captor.getValue().getPasswordHash()).isEqualTo("hash");
		assertThat(captor.getValue().getRole()).isEqualTo(UserRoleENUM.USER);
	}

	@Test
	void registerThrowsWhenEmailActive() {
		User existing = new User();
		existing.setEmail("a@b.com");
		existing.setDeletedAt(null);
		when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(existing));

		assertThatThrownBy(() -> authService.register(new RegisterRequest("Name", "a@b.com", "password12")))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
				.isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void registerReactivatesSoftDeletedUser() {
		User existing = new User();
		existing.setId(3);
		existing.setEmail("a@b.com");
		existing.setDeletedAt(Instant.now());
		when(userRepository.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(existing));
		when(passwordEncoder.encode("password12")).thenReturn("newhash");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		Instant exp = Instant.now().plus(1, ChronoUnit.HOURS);
		when(jwtService.buildAuthResponse(any(User.class))).thenReturn(new AuthResponse("tok", exp));

		authService.register(new RegisterRequest("New", "a@b.com", "password12"));

		assertThat(existing.getDeletedAt()).isNull();
		assertThat(existing.getPasswordHash()).isEqualTo("newhash");
		assertThat(existing.getName()).isEqualTo("New");
		assertThat(existing.getRole()).isEqualTo(UserRoleENUM.USER);
	}

	@Test
	void loginReturnsTokenWhenCredentialsValid() {
		User user = new User();
		user.setId(1);
		user.setEmail("a@b.com");
		user.setPasswordHash("stored");
		user.setRole(UserRoleENUM.USER);
		when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("a@b.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password12", "stored")).thenReturn(true);
		Instant exp = Instant.now().plus(1, ChronoUnit.HOURS);
		when(jwtService.buildAuthResponse(user)).thenReturn(new AuthResponse("tok", exp));

		AuthResponse response = authService.login(new LoginRequest("a@b.com", "password12"));

		assertThat(response.token()).isEqualTo("tok");
	}

	@Test
	void getAuthConfigWhenGoogleClientIdMissing() {
		AuthConfigResponse config = authService.getAuthConfig();

		assertThat(config.googleAuthEnabled()).isFalse();
		assertThat(config.googleWebClientId()).isNull();
	}

	@Test
	void loginFailsWhenUserMissing() {
		when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("a@b.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(new LoginRequest("a@b.com", "password12")))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
				.isEqualTo(HttpStatus.UNAUTHORIZED);
	}
}