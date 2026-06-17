package com.cardly.service;

import com.cardly.domain.Card;
import com.cardly.domain.Deck;
import com.cardly.domain.DifficultyLevelENUM;
import com.cardly.domain.ReviewResultENUM;
import com.cardly.domain.ScheduledIntervalENUM;
import com.cardly.repository.CardRepository;
import com.cardly.repository.StudyReviewEventRepository;
import com.cardly.specification.CardSpecification;
import com.cardly.web.dto.AnswerCardRequest;
import com.cardly.web.dto.CardResponse;
import com.cardly.web.dto.CardSearchRequest;
import com.cardly.web.dto.CreateCardRequest;
import com.cardly.web.dto.PagedResponse;
import com.cardly.web.dto.UpdateCardRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CardService {

	private final CardRepository cardRepository;
	private final StudyReviewService studyReviewService;
	private final StudyReviewEventRepository studyReviewEventRepository;

	public CardService(
			CardRepository cardRepository,
			StudyReviewService studyReviewService,
			StudyReviewEventRepository studyReviewEventRepository) {
		this.cardRepository = cardRepository;
		this.studyReviewService = studyReviewService;
		this.studyReviewEventRepository = studyReviewEventRepository;
	}

	@Transactional(readOnly = true)
	public List<CardResponse> listCards(Integer deckId) {
		return cardRepository.findByDeck_IdAndDeletedAtIsNullOrderByIdAsc(deckId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public PagedResponse<CardResponse> searchCards(Integer userId, Integer deckId, CardSearchRequest request) {
		Pageable pageable = pageable(request.page(), request.size(), Sort.by("id").ascending());
		Specification<Card> spec = buildSearchSpecification(userId, deckId, request);
		Page<CardResponse> page = cardRepository.findAll(spec, pageable).map(this::toResponse);
		return PagedResponse.from(page);
	}

	@Transactional(readOnly = true)
	public PagedResponse<CardResponse> searchAllCards(Integer ownerId, Integer deckId, CardSearchRequest request) {
		Pageable pageable = pageable(request.page(), request.size(), Sort.by("id").ascending());
		Specification<Card> spec = buildSearchSpecification(ownerId, deckId, request);
		Page<CardResponse> page = cardRepository.findAll(spec, pageable).map(this::toResponse);
		return PagedResponse.from(page);
	}

	@Transactional(readOnly = true)
	public List<CardResponse> listScheduledCardsForDeck(Integer userId, Integer deckId) {
		CardSearchRequest request = new CardSearchRequest(null, null, null, true, null, null, 0, 200);
		return searchCards(userId, deckId, request).content();
	}

	@Transactional(readOnly = true)
	public Optional<Card> findCardByIdAndDeck(Integer cardId, Integer deckId) {
		return cardRepository.findByIdAndDeck_IdAndDeletedAtIsNull(cardId, deckId);
	}

	@Transactional(readOnly = true)
	public Optional<Card> findCardById(Integer cardId) {
		return cardRepository.findByIdAndDeletedAtIsNull(cardId);
	}

	@Transactional
	public CardResponse createCard(Deck deck, CreateCardRequest request) {
		Card card = new Card();
		card.setDeck(deck);
		card.setQuestion(request.question());
		card.setAnswer(request.answer());
		card.setDifficultyLevel(DifficultyLevelENUM.NONE);
		card.setRightStreak(0);
		card.setWrongStreak(0);
		card.setDueAt(null);
		card.setScheduledInterval(null);
		Card saved = cardRepository.save(card);
		return toResponse(saved);
	}

	@Transactional
	public CardResponse updateCard(Card card, UpdateCardRequest request) {
		if (StringUtils.hasText(request.question())) {
			card.setQuestion(request.question().trim());
		}
		if (StringUtils.hasText(request.answer())) {
			card.setAnswer(request.answer().trim());
		}
		Card saved = cardRepository.save(card);
		return toResponse(saved);
	}

	@Transactional
	public void softDeleteCard(Card card) {
		if (card.getDeletedAt() != null) {
			return;
		}
		card.setDeletedAt(Instant.now());
	}

	@Transactional
	public CardResponse answerCard(Card card, AnswerCardRequest request) {
		if (request.correct()) {
			int rightStreak = card.getRightStreak() + 1;
			card.setRightStreak(rightStreak);
			card.setWrongStreak(0);
		} else {
			card.setRightStreak(0);
			card.setWrongStreak(card.getWrongStreak() + 1);
		}
		ScheduleTransition transition = transitionForAnswer(card.getDifficultyLevel(), request.correct());
		applySchedule(card, transition.difficulty(), transition.interval());
		Card saved = cardRepository.save(card);
		studyReviewService.registerReview(saved, request.correct() ? ReviewResultENUM.CORRECT : ReviewResultENUM.WRONG);
		return toResponse(saved);
	}

	@Transactional
	public CardResponse skipCard(Card card) {
		DifficultyLevelENUM difficulty = card.getDifficultyLevel() == DifficultyLevelENUM.NONE
				? DifficultyLevelENUM.HARD
				: card.getDifficultyLevel();
		ScheduledIntervalENUM interval = card.getScheduledInterval() == null
				? ScheduledIntervalENUM.DAYS_1
				: card.getScheduledInterval();
		applySchedule(card, difficulty, interval);
		Card saved = cardRepository.save(card);
		studyReviewService.registerReview(saved, ReviewResultENUM.SKIPPED);
		return toResponse(saved);
	}

	public long countCards(Integer userId) {
		return cardRepository.countByDeck_User_IdAndDeletedAtIsNull(userId);
	}

	public long countDueCards(Integer userId) {
		return countDueCardsOnly(userId) + countUnscheduledCards(userId);
	}

	public long countDueCardsOnly(Integer userId) {
		return cardRepository.countByDeck_User_IdAndDeletedAtIsNullAndDueAtLessThanEqual(userId, Instant.now());
	}

	public long countWaitingCards(Integer userId) {
		return cardRepository.countByDeck_User_IdAndDeletedAtIsNullAndDueAtGreaterThan(userId, Instant.now());
	}

	public long countUnscheduledCards(Integer userId) {
		return cardRepository.countByDeck_User_IdAndDeletedAtIsNullAndDueAtIsNull(userId);
	}

	public long countAnsweredToday(Integer userId) {
		Instant startOfTodayUtc = Instant.now().truncatedTo(ChronoUnit.DAYS);
		return studyReviewEventRepository.countByUser_IdAndDeletedAtIsNullAndCreatedAtGreaterThanEqual(userId, startOfTodayUtc);
	}

	private void applySchedule(Card card, DifficultyLevelENUM difficulty, ScheduledIntervalENUM interval) {
		card.setDifficultyLevel(difficulty);
		card.setScheduledInterval(interval);
		var offset = dueOffset(interval);
		card.setDueAt(Instant.now().plus(offset.amount(), offset.unit()));
	}

	private record DueOffset(long amount, ChronoUnit unit) {
	}

	private record ScheduleTransition(DifficultyLevelENUM difficulty, ScheduledIntervalENUM interval) {
	}

	private DueOffset dueOffset(ScheduledIntervalENUM interval) {
		return switch (interval) {
			case HOURS_2 -> new DueOffset(2, ChronoUnit.HOURS);
			case HOURS_4 -> new DueOffset(4, ChronoUnit.HOURS);
			case HOURS_36 -> new DueOffset(36, ChronoUnit.HOURS);
			case DAYS_1 -> new DueOffset(1, ChronoUnit.DAYS);
			case DAYS_2 -> new DueOffset(2, ChronoUnit.DAYS);
			case DAYS_3 -> new DueOffset(3, ChronoUnit.DAYS);
			case DAYS_5 -> new DueOffset(5, ChronoUnit.DAYS);
			case DAYS_7 -> new DueOffset(7, ChronoUnit.DAYS);
			case DAYS_9 -> new DueOffset(9, ChronoUnit.DAYS);
		};
	}

	private ScheduleTransition transitionForAnswer(DifficultyLevelENUM currentDifficulty, boolean correct) {
		DifficultyLevelENUM difficulty = currentDifficulty == null ? DifficultyLevelENUM.NONE : currentDifficulty;
		return switch (difficulty) {
			case NONE -> correct
					? new ScheduleTransition(DifficultyLevelENUM.MEDIUM, ScheduledIntervalENUM.HOURS_4)
					: new ScheduleTransition(DifficultyLevelENUM.HARD, ScheduledIntervalENUM.HOURS_2);
			case MEDIUM -> correct
					? new ScheduleTransition(DifficultyLevelENUM.EASY, ScheduledIntervalENUM.HOURS_36)
					: new ScheduleTransition(DifficultyLevelENUM.HARD, ScheduledIntervalENUM.HOURS_2);
			case HARD -> correct
					? new ScheduleTransition(DifficultyLevelENUM.MEDIUM, ScheduledIntervalENUM.HOURS_4)
					: new ScheduleTransition(DifficultyLevelENUM.HARD, ScheduledIntervalENUM.HOURS_2);
			case EASY -> correct
					? new ScheduleTransition(DifficultyLevelENUM.EASY, ScheduledIntervalENUM.HOURS_36)
					: new ScheduleTransition(DifficultyLevelENUM.MEDIUM, ScheduledIntervalENUM.HOURS_4);
		};
	}

	private CardResponse toResponse(Card card) {
		return new CardResponse(
				card.getId(),
				card.getDeck().getId(),
				card.getQuestion(),
				card.getAnswer(),
				card.getDifficultyLevel(),
				card.getDueAt(),
				card.getRightStreak(),
				card.getWrongStreak(),
				card.getScheduledInterval(),
				card.getCreatedAt(),
				card.getUpdatedAt()
		);
	}

	private Specification<Card> buildSearchSpecification(Integer userId, Integer deckId, CardSearchRequest request) {
		return Specification.where(CardSpecification.active())
				.and(CardSpecification.byOwner(userId))
				.and(CardSpecification.byDeck(deckId))
				.and(CardSpecification.questionContains(request.question()))
				.and(CardSpecification.difficultyEquals(request.difficultyLevel()))
				.and(CardSpecification.dueNow(request.dueOnly()))
				.and(CardSpecification.scheduledOnly(request.scheduledOnly()))
				.and(CardSpecification.waitingOnly(request.waitingOnly()))
				.and(CardSpecification.readyScheduledOnly(request.readyScheduledOnly()));
	}

	private Pageable pageable(Integer page, Integer size, Sort sort) {
		int safePage = page == null || page < 0 ? 0 : page;
		int safeSize = size == null || size < 1 ? 20 : Math.min(size, 100);
		return PageRequest.of(safePage, safeSize, sort);
	}
}
