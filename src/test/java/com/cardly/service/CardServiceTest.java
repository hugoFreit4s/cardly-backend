package com.cardly.service;

import com.cardly.domain.Card;
import com.cardly.domain.Deck;
import com.cardly.domain.DifficultyLevelENUM;
import com.cardly.repository.CardRepository;
import com.cardly.repository.StudyReviewEventRepository;
import com.cardly.domain.ScheduledIntervalENUM;
import com.cardly.web.dto.AnswerCardRequest;
import com.cardly.web.dto.CardResponse;
import com.cardly.web.dto.CreateCardRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

	@Mock
	private CardRepository cardRepository;

	@Mock
	private StudyReviewService studyReviewService;

	@Mock
	private StudyReviewEventRepository studyReviewEventRepository;

	@InjectMocks
	private CardService cardService;

	@Test
	void listCardsReturnsCardsForDeck() {
		Deck deck = new Deck();
		deck.setId(5);
		Card card = createCard(1, deck, "Q1", "A1");
		when(cardRepository.findByDeck_IdAndDeletedAtIsNullOrderByIdAsc(5)).thenReturn(List.of(card));

		List<CardResponse> result = cardService.listCards(5);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).id()).isEqualTo(1);
		assertThat(result.get(0).deckId()).isEqualTo(5);
		assertThat(result.get(0).question()).isEqualTo("Q1");
	}

	@Test
	void createCardSetsDefaultValues() {
		Deck deck = new Deck();
		deck.setId(10);
		when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
			Card c = invocation.getArgument(0);
			c.setId(100);
			c.setCreatedAt(Instant.now());
			c.setUpdatedAt(Instant.now());
			return c;
		});

		CreateCardRequest request = new CreateCardRequest("Capital of France?", "Paris");
		CardResponse response = cardService.createCard(deck, request);

		ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
		verify(cardRepository).save(captor.capture());
		Card saved = captor.getValue();
		assertThat(saved.getDifficultyLevel()).isEqualTo(DifficultyLevelENUM.NONE);
		assertThat(saved.getRightStreak()).isZero();
		assertThat(saved.getWrongStreak()).isZero();
		assertThat(saved.getDueAt()).isNull();
		assertThat(saved.getScheduledInterval()).isNull();
		assertThat(response.question()).isEqualTo("Capital of France?");
		assertThat(response.answer()).isEqualTo("Paris");
	}

	@Test
	void answerCardWrongSchedulesHardForTwoHours() {
		Deck deck = new Deck();
		deck.setId(5);
		Card card = createCard(1, deck, "Q", "A");
		when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CardResponse response = cardService.answerCard(card, new AnswerCardRequest(false));

		assertThat(response.difficultyLevel()).isEqualTo(DifficultyLevelENUM.HARD);
		assertThat(response.scheduledInterval()).isEqualTo(ScheduledIntervalENUM.HOURS_2);
		assertThat(response.dueAt()).isAfter(Instant.now());
		assertThat(response.dueAt()).isBefore(Instant.now().plus(3, ChronoUnit.HOURS));
		assertThat(card.getWrongStreak()).isEqualTo(1);
	}

	@Test
	void answerCardCorrectSchedulesHardForOneDay() {
		Deck deck = new Deck();
		deck.setId(5);
		Card card = createCard(1, deck, "Q", "A");
		when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CardResponse response = cardService.answerCard(card, new AnswerCardRequest(true));

		assertThat(response.difficultyLevel()).isEqualTo(DifficultyLevelENUM.HARD);
		assertThat(response.scheduledInterval()).isEqualTo(ScheduledIntervalENUM.DAYS_1);
		assertThat(response.dueAt()).isAfter(Instant.now().plus(20, ChronoUnit.HOURS));
		assertThat(response.dueAt()).isBefore(Instant.now().plus(2, ChronoUnit.DAYS));
		assertThat(card.getRightStreak()).isEqualTo(1);
	}

	@Test
	void firstStudyPassMarksAllCardsHardWhenFourCorrectAndOneWrong() {
		Deck deck = new Deck();
		deck.setId(5);
		when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Card[] cards = new Card[5];
		for (int i = 0; i < 5; i++) {
			cards[i] = createCard(i + 1, deck, "Q" + i, "A" + i);
		}

		for (int i = 0; i < 4; i++) {
			CardResponse response = cardService.answerCard(cards[i], new AnswerCardRequest(true));
			assertThat(response.difficultyLevel()).isEqualTo(DifficultyLevelENUM.HARD);
			assertThat(response.scheduledInterval()).isEqualTo(ScheduledIntervalENUM.DAYS_1);
			assertThat(response.dueAt()).isAfter(Instant.now());
		}

		CardResponse wrongResponse = cardService.answerCard(cards[4], new AnswerCardRequest(false));
		assertThat(wrongResponse.difficultyLevel()).isEqualTo(DifficultyLevelENUM.HARD);
		assertThat(wrongResponse.scheduledInterval()).isEqualTo(ScheduledIntervalENUM.HOURS_2);
		assertThat(wrongResponse.dueAt()).isAfter(Instant.now());
	}

	@Test
	void answerCardCorrectIncrementsStreakAndKeepsOneDaySchedule() {
		Deck deck = new Deck();
		deck.setId(5);
		Card card = createCard(1, deck, "Q", "A");
		card.setRightStreak(1);
		when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CardResponse response = cardService.answerCard(card, new AnswerCardRequest(true));

		assertThat(response.difficultyLevel()).isEqualTo(DifficultyLevelENUM.HARD);
		assertThat(response.scheduledInterval()).isEqualTo(ScheduledIntervalENUM.DAYS_1);
		assertThat(card.getRightStreak()).isEqualTo(2);
	}

	private Card createCard(Integer id, Deck deck, String question, String answer) {
		Card card = new Card();
		card.setId(id);
		card.setDeck(deck);
		card.setQuestion(question);
		card.setAnswer(answer);
		card.setDifficultyLevel(DifficultyLevelENUM.NONE);
		card.setRightStreak(0);
		card.setWrongStreak(0);
		card.setCreatedAt(Instant.now());
		card.setUpdatedAt(Instant.now());
		return card;
	}
}
