package com.cardly.web;

import com.cardly.domain.Deck;
import com.cardly.security.UserPrincipal;
import com.cardly.service.CardService;
import com.cardly.service.DeckService;
import com.cardly.web.dto.AnswerCardRequest;
import com.cardly.web.dto.CardSearchRequest;
import com.cardly.web.dto.CardResponse;
import com.cardly.web.dto.CreateCardRequest;
import com.cardly.web.dto.PagedResponse;
import com.cardly.web.dto.UpdateCardRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/decks/{deckId}/cards")
public class CardController {

	private final CardService cardService;
	private final DeckService deckService;

	public CardController(CardService cardService, DeckService deckService) {
		this.cardService = cardService;
		this.deckService = deckService;
	}

	@GetMapping
	public List<CardResponse> listCards(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer deckId) {
		Deck deck = deckService.findDeckByIdAndUser(deckId, principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return cardService.listCards(deck.getId());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CardResponse createCard(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer deckId,
			@Valid @RequestBody CreateCardRequest request) {
		Deck deck = deckService.findDeckByIdAndUser(deckId, principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return cardService.createCard(deck, request);
	}

	@PostMapping("/search")
	public PagedResponse<CardResponse> searchCards(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer deckId,
			@RequestBody(required = false) CardSearchRequest request) {
		Deck deck = deckService.findDeckByIdAndUser(deckId, principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		CardSearchRequest safeRequest = request == null
				? new CardSearchRequest(null, null, null, null, null, null, 0, 20)
				: request;
		return cardService.searchCards(principal.getId(), deck.getId(), safeRequest);
	}

	@PutMapping("/{cardId}")
	public CardResponse updateCard(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer deckId,
			@PathVariable Integer cardId,
			@RequestBody UpdateCardRequest request) {
		deckService.findDeckByIdAndUser(deckId, principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		var card = cardService.findCardByIdAndDeck(cardId, deckId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return cardService.updateCard(card, request);
	}

	@DeleteMapping("/{cardId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCard(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer deckId,
			@PathVariable Integer cardId) {
		deckService.findDeckByIdAndUser(deckId, principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		var card = cardService.findCardByIdAndDeck(cardId, deckId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		cardService.softDeleteCard(card);
	}

	@PostMapping("/{cardId}/answer")
	public CardResponse answerCard(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer deckId,
			@PathVariable Integer cardId,
			@RequestBody AnswerCardRequest request) {
		deckService.findDeckByIdAndUser(deckId, principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		var card = cardService.findCardByIdAndDeck(cardId, deckId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return cardService.answerCard(card, request);
	}

	@PostMapping("/{cardId}/skip")
	public CardResponse skipCard(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer deckId,
			@PathVariable Integer cardId) {
		deckService.findDeckByIdAndUser(deckId, principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		var card = cardService.findCardByIdAndDeck(cardId, deckId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return cardService.skipCard(card);
	}
}
