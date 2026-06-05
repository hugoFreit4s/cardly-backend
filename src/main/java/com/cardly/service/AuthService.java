package com.cardly.service;

import com.cardly.domain.User;
import com.cardly.domain.UserRoleENUM;
import com.cardly.repository.UserRepository;
import com.cardly.util.EmailNormalizer;
import com.cardly.web.dto.AuthConfigResponse;
import com.cardly.web.dto.AuthResponse;
import com.cardly.web.dto.GoogleLoginRequest;
import com.cardly.web.dto.LoginRequest;
import com.cardly.web.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PublicIdService publicIdService;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final String googleClientId;

	public AuthService(
			UserRepository userRepository,
			PublicIdService publicIdService,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			ObjectMapper objectMapper,
			@Value("${cardly.google.client-id:}") String googleClientId) {
		this.userRepository = userRepository;
		this.publicIdService = publicIdService;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.objectMapper = objectMapper;
		this.googleClientId = googleClientId == null ? "" : googleClientId.trim();
		this.httpClient = HttpClient.newHttpClient();
	}

	public AuthResponse register(RegisterRequest request) {
		String email = EmailNormalizer.normalize(request.email());
		Optional<User> existing = userRepository.findByEmailIgnoreCase(email);
		if (existing.isPresent()) {
			User user = existing.get();
			if (user.getDeletedAt() == null) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
			}
			user.setEmail(email);
			user.setPasswordHash(passwordEncoder.encode(request.password()));
			user.setName(request.name());
			user.setDeletedAt(null);
			user.setRole(UserRoleENUM.USER);
			publicIdService.ensurePublicId(user);
			User saved = userRepository.save(user);
			return jwtService.buildAuthResponse(saved);
		}
		User user = new User();
		user.setName(request.name());
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setRole(UserRoleENUM.USER);
		publicIdService.ensurePublicId(user);
		User saved = userRepository.save(user);
		return jwtService.buildAuthResponse(saved);
	}

	public AuthResponse login(LoginRequest request) {
		String email = EmailNormalizer.normalize(request.email());
		User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
		return jwtService.buildAuthResponse(user);
	}

	public AuthConfigResponse getAuthConfig() {
		boolean enabled = !googleClientId.isEmpty();
		return new AuthConfigResponse(enabled, enabled ? googleClientId : null);
	}

	public AuthResponse googleLogin(GoogleLoginRequest request) {
		GoogleIdentity identity = fetchGoogleIdentity(request.idToken());
		String email = EmailNormalizer.normalize(identity.email());
		Optional<User> existing = userRepository.findByEmailIgnoreCase(email);
		User user;
		if (existing.isPresent()) {
			user = existing.get();
			user.setEmail(email);
			user.setDeletedAt(null);
			if (!org.springframework.util.StringUtils.hasText(user.getName())) {
				user.setName(identity.name());
			}
			if (user.getRole() == null) {
				user.setRole(UserRoleENUM.USER);
			}
		} else {
			user = new User();
			user.setEmail(email);
			user.setName(identity.name());
			user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
			user.setRole(UserRoleENUM.USER);
		}
		publicIdService.ensurePublicId(user);
		User saved = userRepository.save(user);
		return jwtService.buildAuthResponse(saved);
	}

	private GoogleIdentity fetchGoogleIdentity(String idToken) {
		String encoded = URLEncoder.encode(idToken, StandardCharsets.UTF_8);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + encoded))
				.GET()
				.build();
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 400) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
			}
			JsonNode node = objectMapper.readTree(response.body());
			String email = node.path("email").asText(null);
			String name = node.path("name").asText("Google User");
			String emailVerified = node.path("email_verified").asText("false");
			String audience = node.path("aud").asText(null);
			if (email == null || !"true".equalsIgnoreCase(emailVerified)) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account email is not verified");
			}
			if (!googleClientId.isEmpty() && !googleClientId.equals(audience)) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google token not issued for this app");
			}
			return new GoogleIdentity(email, name);
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unable to validate Google token");
		}
	}

	private record GoogleIdentity(String email, String name) {
	}
}
