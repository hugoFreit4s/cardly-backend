package com.cardly.service;

import com.cardly.domain.Card;
import com.cardly.domain.Deck;
import com.cardly.domain.User;
import com.cardly.domain.UserRoleENUM;
import com.cardly.repository.CardRepository;
import com.cardly.repository.DeckRepository;
import com.cardly.repository.UserRepository;
import com.cardly.specification.UserSpecification;
import com.cardly.util.EmailNormalizer;
import com.cardly.web.dto.AdminCreateUserRequest;
import com.cardly.web.dto.PagedResponse;
import com.cardly.web.dto.UpdateProfileRequest;
import com.cardly.web.dto.UpdateUserRequest;
import com.cardly.web.dto.UserResponse;
import com.cardly.web.dto.UserSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final DeckRepository deckRepository;
	private final CardRepository cardRepository;
	private final PasswordEncoder passwordEncoder;
	private final PublicIdService publicIdService;

	public UserService(
			UserRepository userRepository,
			DeckRepository deckRepository,
			CardRepository cardRepository,
			PasswordEncoder passwordEncoder,
			PublicIdService publicIdService) {
		this.userRepository = userRepository;
		this.deckRepository = deckRepository;
		this.cardRepository = cardRepository;
		this.passwordEncoder = passwordEncoder;
		this.publicIdService = publicIdService;
	}

	@Transactional
	public void softDeleteUser(Integer userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (user.getDeletedAt() != null) {
			return;
		}
		Instant now = Instant.now();
		user.setDeletedAt(now);
		List<Deck> decks = deckRepository.findByUser_IdAndDeletedAtIsNull(userId);
		for (Deck deck : decks) {
			deck.setDeletedAt(now);
			List<Card> cards = cardRepository.findByDeck_IdAndDeletedAtIsNull(deck.getId());
			for (Card card : cards) {
				card.setDeletedAt(now);
			}
		}
	}

	@Transactional
	public void softDeleteActiveUser(Integer userId) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		softDeleteUser(user.getId());
	}

	@Transactional(readOnly = true)
	public Optional<User> findActiveUserById(Integer userId) {
		return userRepository.findByIdAndDeletedAtIsNull(userId);
	}

	@Transactional(readOnly = true)
	public PagedResponse<UserResponse> searchUsers(UserSearchRequest request, Integer excludeUserId) {
		Pageable pageable = pageable(request.page(), request.size(), Sort.by("id").ascending());
		Specification<User> spec = Specification.where(UserSpecification.active())
				.and(UserSpecification.nameContains(request.name()))
				.and(UserSpecification.emailContains(request.email()))
				.and(UserSpecification.roleEquals(request.role()))
				.and(UserSpecification.excludeId(excludeUserId));
		Page<UserResponse> page = userRepository.findAll(spec, pageable).map(this::toResponse);
		return PagedResponse.from(page);
	}

	@Transactional
	public UserResponse createUserByAdmin(AdminCreateUserRequest request) {
		String email = EmailNormalizer.normalize(request.email());
		UserRoleENUM role = request.role() == null ? UserRoleENUM.USER : request.role();
		Optional<User> existing = userRepository.findByEmailIgnoreCase(email);
		if (existing.isPresent()) {
			User user = existing.get();
			user.setEmail(email);
			user.setName(request.name().trim());
			user.setPasswordHash(passwordEncoder.encode(request.password()));
			user.setRole(role);
			user.setDeletedAt(null);
			publicIdService.ensurePublicId(user);
			return toResponse(userRepository.save(user));
		}
		User user = new User();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setRole(role);
		publicIdService.ensurePublicId(user);
		return toResponse(userRepository.save(user));
	}

	@Transactional
	public User updateOwnProfile(Integer userId, UpdateProfileRequest request) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (StringUtils.hasText(request.name())) {
			user.setName(request.name().trim());
		}
		if (StringUtils.hasText(request.newPassword())) {
			if (request.newPassword().length() < 8) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nova senha deve ter ao menos 8 caracteres");
			}
			if (!StringUtils.hasText(request.currentPassword())
					|| !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta");
			}
			user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		}
		return userRepository.save(user);
	}

	@Transactional
	public UserResponse updateUser(User user, UpdateUserRequest request) {
		if (StringUtils.hasText(request.name())) {
			user.setName(request.name().trim());
		}
		if (request.role() != null) {
			user.setRole(request.role());
		}
		return toResponse(userRepository.save(user));
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getPublicId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}

	private Pageable pageable(Integer page, Integer size, Sort sort) {
		int safePage = page == null || page < 0 ? 0 : page;
		int safeSize = size == null || size < 1 ? 20 : Math.min(size, 100);
		return PageRequest.of(safePage, safeSize, sort);
	}
}
