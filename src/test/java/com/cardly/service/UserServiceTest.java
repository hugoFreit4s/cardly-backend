package com.cardly.service;

import com.cardly.domain.Card;
import com.cardly.domain.Deck;
import com.cardly.domain.User;
import com.cardly.repository.CardRepository;
import com.cardly.repository.DeckRepository;
import com.cardly.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private DeckRepository deckRepository;

	@Mock
	private CardRepository cardRepository;

	@Mock
	private PublicIdService publicIdService;

	@InjectMocks
	private UserService userService;

	@Test
	void softDeleteUserSetsDeletedAtOnUserDecksAndCards() {
		User user = new User();
		user.setId(1);
		user.setDeletedAt(null);
		when(userRepository.findById(1)).thenReturn(Optional.of(user));

		Deck deck = new Deck();
		deck.setId(10);
		deck.setUser(user);
		when(deckRepository.findByUser_IdAndDeletedAtIsNull(1)).thenReturn(List.of(deck));

		Card card = new Card();
		card.setId(100);
		card.setDeck(deck);
		when(cardRepository.findByDeck_IdAndDeletedAtIsNull(10)).thenReturn(List.of(card));

		userService.softDeleteUser(1);

		assertThat(user.getDeletedAt()).isNotNull();
		assertThat(deck.getDeletedAt()).isEqualTo(user.getDeletedAt());
		assertThat(card.getDeletedAt()).isEqualTo(user.getDeletedAt());
		verify(userRepository).findById(1);
	}

	@Test
	void softDeleteUserThrowsWhenMissing() {
		when(userRepository.findById(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.softDeleteUser(99))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void softDeleteUserNoOpWhenAlreadyDeleted() {
		User user = new User();
		user.setId(1);
		Instant prior = Instant.now().minusSeconds(60);
		user.setDeletedAt(prior);
		when(userRepository.findById(1)).thenReturn(Optional.of(user));

		userService.softDeleteUser(1);

		assertThat(user.getDeletedAt()).isEqualTo(prior);
	}
}