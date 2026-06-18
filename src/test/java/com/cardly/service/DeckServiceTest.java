package com.cardly.service;

import com.cardly.domain.Card;
import com.cardly.domain.Deck;
import com.cardly.domain.User;
import com.cardly.repository.CardRepository;
import com.cardly.repository.DeckRepository;
import com.cardly.repository.UserRepository;
import com.cardly.web.dto.DeckSearchRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

	@Mock
	private DeckRepository deckRepository;

	@Mock
	private CardRepository cardRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private DeckService deckService;

	@Test
	void cloneDeckToUserRejectsDuplicateClone() {
		Deck sourceDeck = new Deck();
		sourceDeck.setId(10);
		sourceDeck.setName("Matemática");
		sourceDeck.setSubject("Matemática");

		Deck existingClone = new Deck();
		existingClone.setId(99);

		when(deckRepository.findByUser_IdAndSourceDeck_IdAndDeletedAtIsNull(5, 10))
				.thenReturn(Optional.of(existingClone));

		assertThatThrownBy(() -> deckService.cloneDeckToUser(sourceDeck, 5))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("already cloned");

		verify(deckRepository, never()).save(any(Deck.class));
		verify(cardRepository, never()).saveAll(any());
	}

	@Test
	void searchCommunityDecksShowsCloneActionWhenUserHasNoActiveClone() {
		User owner = new User();
		owner.setId(2);
		owner.setName("Maria");

		Deck publicDeck = new Deck();
		publicDeck.setId(10);
		publicDeck.setName("Geografia BR");
		publicDeck.setSubject("Geografia");
		publicDeck.setPosition(1);
		publicDeck.setPublic(true);
		publicDeck.setUser(owner);
		publicDeck.setCreatedAt(Instant.parse("2026-06-01T12:00:00Z"));
		publicDeck.setUpdatedAt(Instant.parse("2026-06-01T12:00:00Z"));

		when(deckRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(publicDeck)));
		when(cardRepository.countByDeck_IdAndDeletedAtIsNull(10)).thenReturn(3);
		when(cardRepository.countByDeck_IdAndDeletedAtIsNullAndDueAtIsNotNull(10)).thenReturn(0);
		when(cardRepository.countByDeck_IdAndDeletedAtIsNullAndDueAtGreaterThan(eq(10), any(Instant.class)))
				.thenReturn(0);
		when(cardRepository.countByDeck_IdAndDeletedAtIsNullAndDueAtIsNotNullAndDueAtLessThanEqual(eq(10), any(Instant.class)))
				.thenReturn(0);
		when(deckRepository.findByUser_IdAndSourceDeck_IdAndDeletedAtIsNull(5, 10))
				.thenReturn(Optional.empty());

		var response = deckService.searchCommunityDecks(5, new DeckSearchRequest(null, null, true, null, 0, 20));

		assertThat(response.content()).hasSize(1);
		assertThat(response.content().get(0).alreadyCloned()).isFalse();
		assertThat(response.content().get(0).clonedDeckId()).isNull();
	}

	@Test
	void softDeleteDeckSetsDeletedAtOnManagedDeckAndCards() {
		Deck detached = new Deck();
		detached.setId(10);

		Deck managed = new Deck();
		managed.setId(10);
		managed.setDeletedAt(null);

		Card card = new Card();
		card.setId(100);
		card.setDeck(managed);

		when(deckRepository.findById(10)).thenReturn(Optional.of(managed));
		when(cardRepository.findByDeck_IdAndDeletedAtIsNull(10)).thenReturn(List.of(card));

		deckService.softDeleteDeck(detached);

		assertThat(managed.getDeletedAt()).isNotNull();
		assertThat(card.getDeletedAt()).isEqualTo(managed.getDeletedAt());
		verify(deckRepository).findById(10);
	}
}
