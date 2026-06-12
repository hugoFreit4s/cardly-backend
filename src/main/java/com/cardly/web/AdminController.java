package com.cardly.web;

import com.cardly.security.UserPrincipal;
import com.cardly.service.CardService;
import com.cardly.service.DeckService;
import com.cardly.service.UserService;
import com.cardly.web.dto.AdminCardSearchRequest;
import com.cardly.web.dto.AdminCreateCardRequest;
import com.cardly.web.dto.AdminCreateDeckRequest;
import com.cardly.web.dto.AdminCreateUserRequest;
import com.cardly.web.dto.CardResponse;
import com.cardly.web.dto.CardSearchRequest;
import com.cardly.web.dto.CreateCardRequest;
import com.cardly.web.dto.CreateDeckRequest;
import com.cardly.web.dto.DeckSearchRequest;
import com.cardly.web.dto.DeckResponse;
import com.cardly.web.dto.PagedResponse;
import com.cardly.web.dto.UpdateCardRequest;
import com.cardly.web.dto.UpdateDeckRequest;
import com.cardly.web.dto.UpdateUserRequest;
import com.cardly.web.dto.UserResponse;
import com.cardly.web.dto.UserSearchRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final UserService userService;
	private final DeckService deckService;
	private final CardService cardService;

	public AdminController(UserService userService, DeckService deckService, CardService cardService) {
		this.userService = userService;
		this.deckService = deckService;
		this.cardService = cardService;
	}

	@PostMapping("/users/search")
	public PagedResponse<UserResponse> searchUsers(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestBody(required = false) UserSearchRequest request) {
		UserSearchRequest safeRequest = request == null ? new UserSearchRequest(null, null, null, 0, 20) : request;
		return userService.searchUsers(safeRequest, principal.getId());
	}

	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse createUser(@Valid @RequestBody AdminCreateUserRequest request) {
		return userService.createUserByAdmin(request);
	}

	@PutMapping("/users/{userId}")
	public UserResponse updateUser(@PathVariable Integer userId, @RequestBody UpdateUserRequest request) {
		var user = userService.findActiveUserById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return userService.updateUser(user, request);
	}

	@DeleteMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable Integer userId) {
		userService.softDeleteActiveUser(userId);
	}

	@PostMapping("/subjects/search")
	public PagedResponse<DeckResponse> searchSubjects(@RequestBody(required = false) DeckSearchRequest request) {
		DeckSearchRequest safeRequest = request == null ? new DeckSearchRequest(null, null, null, null, 0, 20) : request;
		return deckService.searchAllDecks(safeRequest);
	}

	@PostMapping("/subjects")
	@ResponseStatus(HttpStatus.CREATED)
	public DeckResponse createSubject(@Valid @RequestBody AdminCreateDeckRequest request) {
		CreateDeckRequest createRequest = new CreateDeckRequest(
				request.name(),
				request.subject(),
				request.isPublic(),
				request.position());
		return deckService.createDeck(request.ownerId(), createRequest);
	}

	@PutMapping("/subjects/{deckId}")
	public DeckResponse updateSubject(@PathVariable Integer deckId, @RequestBody UpdateDeckRequest request) {
		var deck = deckService.findDeckById(deckId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return deckService.updateDeck(deck, request);
	}

	@DeleteMapping("/subjects/{deckId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteSubject(@PathVariable Integer deckId) {
		var deck = deckService.findDeckById(deckId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		deckService.softDeleteDeck(deck);
	}

	@PostMapping("/cards/search")
	public PagedResponse<CardResponse> searchCards(@RequestBody(required = false) AdminCardSearchRequest request) {
		AdminCardSearchRequest safeRequest = request == null
				? new AdminCardSearchRequest(null, null, null, null, null, 0, 20)
				: request;
		CardSearchRequest searchRequest = new CardSearchRequest(
				safeRequest.question(),
				safeRequest.difficultyLevel(),
				safeRequest.dueOnly(),
				null,
				null,
				null,
				safeRequest.page(),
				safeRequest.size());
		return cardService.searchAllCards(safeRequest.ownerId(), safeRequest.deckId(), searchRequest);
	}

	@PostMapping("/cards")
	@ResponseStatus(HttpStatus.CREATED)
	public CardResponse createCard(@Valid @RequestBody AdminCreateCardRequest request) {
		var deck = deckService.findDeckById(request.deckId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return cardService.createCard(deck, new CreateCardRequest(request.question(), request.answer()));
	}

	@PutMapping("/cards/{cardId}")
	public CardResponse updateCard(@PathVariable Integer cardId, @RequestBody UpdateCardRequest request) {
		var card = cardService.findCardById(cardId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		return cardService.updateCard(card, request);
	}

	@DeleteMapping("/cards/{cardId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCard(@PathVariable Integer cardId) {
		var card = cardService.findCardById(cardId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		cardService.softDeleteCard(card);
	}
}
