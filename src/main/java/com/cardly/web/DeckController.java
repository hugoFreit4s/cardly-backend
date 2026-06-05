package com.cardly.web;

import com.cardly.security.UserPrincipal;
import com.cardly.service.DeckService;
import com.cardly.web.dto.CreateDeckRequest;
import com.cardly.web.dto.DeckSearchRequest;
import com.cardly.web.dto.DeckResponse;
import com.cardly.web.dto.PagedResponse;
import com.cardly.web.dto.UpdateDeckRequest;
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
@RequestMapping("/api/decks")
public class DeckController {

	private final DeckService deckService;

	public DeckController(DeckService deckService) {
		this.deckService = deckService;
	}

	@GetMapping
	public List<DeckResponse> listDecks(@AuthenticationPrincipal UserPrincipal principal) {
		return deckService.listDecks(principal.getId());
	}

	@PostMapping("/search")
	public PagedResponse<DeckResponse> searchDecks(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestBody(required = false) DeckSearchRequest request) {
		DeckSearchRequest safeRequest = request == null ? new DeckSearchRequest(null, null, null, null, 0, 20) : request;
		return deckService.searchDecks(principal.getId(), safeRequest);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public DeckResponse createDeck(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody CreateDeckRequest request) {
		return deckService.createDeck(principal.getId(), request);
	}

	@PutMapping("/{deckId}")
	public DeckResponse updateDeck(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer deckId,
			@RequestBody UpdateDeckRequest request) {
		var deck = deckService.findDeckByIdAndUser(deckId, principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return deckService.updateDeck(deck, request);
	}

	@DeleteMapping("/{deckId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDeck(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer deckId) {
		var deck = deckService.findDeckByIdAndUser(deckId, principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		deckService.softDeleteDeck(deck);
	}
}
