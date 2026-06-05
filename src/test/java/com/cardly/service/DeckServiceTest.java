package com.cardly.service;

import com.cardly.domain.Card;
import com.cardly.domain.Deck;
import com.cardly.domain.DifficultyLevelENUM;
import com.cardly.domain.User;
import com.cardly.repository.CardRepository;
import com.cardly.repository.DeckRepository;
import com.cardly.repository.UserRepository;
import com.cardly.web.dto.DeckResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
}
