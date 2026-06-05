package com.cardly.service;

import com.cardly.domain.Card;
import com.cardly.domain.Deck;
import com.cardly.domain.User;
import com.cardly.domain.DifficultyLevelENUM;
import com.cardly.repository.CardRepository;
import com.cardly.repository.DeckRepository;
import com.cardly.repository.UserRepository;
import com.cardly.specification.DeckSpecification;
import com.cardly.web.dto.CreateDeckRequest;
import com.cardly.web.dto.DashboardSubjectStackResponse;
import com.cardly.web.dto.DeckSearchRequest;
import com.cardly.web.dto.DeckResponse;
import com.cardly.web.dto.PagedResponse;
import com.cardly.web.dto.UpdateDeckRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DeckService {

	private final DeckRepository deckRepository;
	private final CardRepository cardRepository;
	private final UserRepository userRepository;

	public DeckService(DeckRepository deckRepository, CardRepository cardRepository, UserRepository userRepository) {
		this.deckRepository = deckRepository;
		this.cardRepository = cardRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<DeckResponse> listDecks(Integer userId) {
		return deckRepository.findByUser_IdAndDeletedAtIsNullOrderByPositionAscIdAsc(userId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public PagedResponse<DeckResponse> searchDecks(Integer userId, DeckSearchRequest request) {
		Pageable pageable = pageable(request.page(), request.size(), Sort.by("position").ascending().and(Sort.by("id").ascending()));
		Specification<Deck> spec = Specification.where(DeckSpecification.active())
				.and(DeckSpecification.byOwner(userId))
				.and(DeckSpecification.nameContains(request.name()))
				.and(DeckSpecification.subjectContains(request.subject()))
				.and(DeckSpecification.isPublic(request.isPublic()));
		Page<DeckResponse> page = deckRepository.findAll(spec, pageable).map(this::toResponse);
		return PagedResponse.from(page);
	}

	@Transactional(readOnly = true)
	public PagedResponse<DeckResponse> searchAllDecks(DeckSearchRequest request) {
		Pageable pageable = pageable(request.page(), request.size(), Sort.by("id").ascending());
		Specification<Deck> spec = Specification.where(DeckSpecification.active())
				.and(DeckSpecification.byOwner(request.ownerId()))
				.and(DeckSpecification.nameContains(request.name()))
				.and(DeckSpecification.subjectContains(request.subject()))
				.and(DeckSpecification.isPublic(request.isPublic()));
		Page<DeckResponse> page = deckRepository.findAll(spec, pageable).map(this::toResponse);
		return PagedResponse.from(page);
	}

	@Transactional(readOnly = true)
	public PagedResponse<DeckResponse> searchCommunityDecks(Integer currentUserId, DeckSearchRequest request) {
		Pageable pageable = pageable(request.page(), request.size(), Sort.by("updatedAt").descending().and(Sort.by("id").descending()));
		Specification<Deck> spec = Specification.where(DeckSpecification.active())
				.and(DeckSpecification.isPublic(true))
				.and(DeckSpecification.excludeOwner(currentUserId))
				.and(DeckSpecification.nameContains(request.name()))
				.and(DeckSpecification.subjectContains(request.subject()));
		Page<DeckResponse> page = deckRepository.findAll(spec, pageable)
				.map(deck -> toCommunityResponse(deck, currentUserId));
		return PagedResponse.from(page);
	}

	@Transactional(readOnly = true)
	public Optional<Deck> findDeckByIdAndUser(Integer deckId, Integer userId) {
		return deckRepository.findByIdAndUser_IdAndDeletedAtIsNull(deckId, userId);
	}

	@Transactional(readOnly = true)
	public Optional<Deck> findPublicDeckById(Integer deckId) {
		return deckRepository.findByIdAndDeletedAtIsNullAndIsPublicTrue(deckId);
	}

	@Transactional(readOnly = true)
	public Optional<Deck> findDeckById(Integer deckId) {
		return deckRepository.findById(deckId).filter(d -> d.getDeletedAt() == null);
	}

	@Transactional
	public DeckResponse createDeck(Integer userId, CreateDeckRequest request) {
		User user = userRepository.getReferenceById(userId);
		Deck deck = new Deck();
		deck.setUser(user);
		deck.setName(request.name());
		deck.setSubject(request.subject());
		deck.setPublic(Boolean.TRUE.equals(request.isPublic()));
		if (request.position() != null) {
			deck.setPosition(request.position());
		} else {
			Integer maxPosition = deckRepository.findMaxPositionByUserId(userId);
			deck.setPosition(maxPosition + 1);
		}
		Deck saved = deckRepository.save(deck);
		return toResponseWithCardCount(saved, 0);
	}

	@Transactional
	public DeckResponse updateDeck(Deck deck, UpdateDeckRequest request) {
		if (StringUtils.hasText(request.name())) {
			deck.setName(request.name().trim());
		}
		if (StringUtils.hasText(request.subject())) {
			deck.setSubject(request.subject().trim());
		}
		if (request.isPublic() != null) {
			deck.setPublic(request.isPublic());
		}
		if (request.position() != null) {
			deck.setPosition(request.position());
		}
		Deck saved = deckRepository.save(deck);
		return toResponse(saved);
	}

	@Transactional
	public void softDeleteDeck(Deck deck) {
		if (deck.getDeletedAt() != null) {
			return;
		}
		Instant now = Instant.now();
		deck.setDeletedAt(now);
		List<Card> cards = cardRepository.findByDeck_IdAndDeletedAtIsNull(deck.getId());
		for (Card card : cards) {
			card.setDeletedAt(now);
		}
	}

	@Transactional
	public DeckResponse cloneDeckToUser(Deck sourceDeck, Integer targetUserId) {
		deckRepository.findByUser_IdAndSourceDeck_IdAndDeletedAtIsNull(targetUserId, sourceDeck.getId())
				.ifPresent(existing -> {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "You already cloned this subject");
				});
		User user = userRepository.getReferenceById(targetUserId);
		Deck clone = new Deck();
		clone.setUser(user);
		clone.setName(sourceDeck.getName());
		clone.setSubject(sourceDeck.getSubject());
		clone.setPublic(false);
		clone.setSourceDeck(sourceDeck);
		Integer maxPosition = deckRepository.findMaxPositionByUserId(targetUserId);
		clone.setPosition(maxPosition + 1);
		Deck savedDeck = deckRepository.save(clone);
		List<Card> sourceCards = cardRepository.findByDeck_IdAndDeletedAtIsNullOrderByIdAsc(sourceDeck.getId());
		List<Card> clonedCards = new ArrayList<>();
		for (Card sourceCard : sourceCards) {
			Card card = new Card();
			card.setDeck(savedDeck);
			card.setQuestion(sourceCard.getQuestion());
			card.setAnswer(sourceCard.getAnswer());
			card.setDifficultyLevel(DifficultyLevelENUM.NONE);
			card.setRightStreak(0);
			card.setWrongStreak(0);
			card.setDueAt(null);
			card.setScheduledInterval(null);
			clonedCards.add(card);
		}
		cardRepository.saveAll(clonedCards);
		return toResponseWithCardCount(savedDeck, clonedCards.size());
	}

	@Transactional(readOnly = true)
	public List<DashboardSubjectStackResponse> summarizeSubjects(Integer userId) {
		List<DeckResponse> decks = listDecks(userId);
		Map<String, SubjectAccumulator> subjectAccumulatorMap = new HashMap<>();
		for (DeckResponse deck : decks) {
			String key = normalizeSubject(deck.subject());
			SubjectAccumulator accumulator = subjectAccumulatorMap.computeIfAbsent(key, ignored -> new SubjectAccumulator());
			accumulator.totalCards += deck.cardCount();
			accumulator.dueCards += deck.readyRevisionCount();
			accumulator.scheduledCards += deck.waitingCardCount();
			accumulator.unscheduledCards += Math.max(deck.cardCount() - deck.scheduledCardCount(), 0);
		}
		return subjectAccumulatorMap.entrySet().stream()
				.map(entry -> new DashboardSubjectStackResponse(
						entry.getKey(),
						entry.getValue().totalCards,
						entry.getValue().dueCards,
						entry.getValue().scheduledCards,
						entry.getValue().unscheduledCards
				))
				.sorted(Comparator.comparing(DashboardSubjectStackResponse::totalCards).reversed()
						.thenComparing(DashboardSubjectStackResponse::subject, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	public long countSubjects(Integer userId) {
		return deckRepository.countByUser_IdAndDeletedAtIsNull(userId);
	}

	private String normalizeSubject(String subject) {
		return StringUtils.hasText(subject) ? subject.trim() : "Sem disciplina";
	}

	private static final class SubjectAccumulator {
		private long totalCards;
		private long dueCards;
		private long scheduledCards;
		private long unscheduledCards;
	}

	private DeckResponse toResponse(Deck deck) {
		int cardCount = cardRepository.countByDeck_IdAndDeletedAtIsNull(deck.getId());
		return toResponseWithCardCount(deck, cardCount, false, null);
	}

	private DeckResponse toCommunityResponse(Deck deck, Integer currentUserId) {
		int cardCount = cardRepository.countByDeck_IdAndDeletedAtIsNull(deck.getId());
		Optional<Deck> existingClone = deckRepository.findByUser_IdAndSourceDeck_IdAndDeletedAtIsNull(
				currentUserId,
				deck.getId());
		return toResponseWithCardCount(
				deck,
				cardCount,
				existingClone.isPresent(),
				existingClone.map(Deck::getId).orElse(null));
	}

	private DeckResponse toResponseWithCardCount(Deck deck, int cardCount, boolean alreadyCloned, Integer clonedDeckId) {
		Instant now = Instant.now();
		int scheduledCardCount = cardRepository.countByDeck_IdAndDeletedAtIsNullAndDueAtIsNotNull(deck.getId());
		int waitingCardCount = cardRepository.countByDeck_IdAndDeletedAtIsNullAndDueAtGreaterThan(deck.getId(), now);
		int readyRevisionCount = cardRepository.countByDeck_IdAndDeletedAtIsNullAndDueAtIsNotNullAndDueAtLessThanEqual(deck.getId(), now);
		String ownerName = deck.getUser() != null ? deck.getUser().getName() : null;
		return new DeckResponse(
				deck.getId(),
				deck.getName(),
				deck.getPosition(),
				deck.getSubject(),
				deck.isPublic(),
				cardCount,
				scheduledCardCount,
				waitingCardCount,
				readyRevisionCount,
				ownerName,
				deck.getCreatedAt(),
				deck.getUpdatedAt(),
				alreadyCloned,
				clonedDeckId
		);
	}

	private DeckResponse toResponseWithCardCount(Deck deck, int cardCount) {
		return toResponseWithCardCount(deck, cardCount, false, null);
	}

	private Pageable pageable(Integer page, Integer size, Sort sort) {
		int safePage = page == null || page < 0 ? 0 : page;
		int safeSize = size == null || size < 1 ? 20 : Math.min(size, 100);
		return PageRequest.of(safePage, safeSize, sort);
	}
}
