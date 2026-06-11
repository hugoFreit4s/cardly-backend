package com.cardly.web;

import com.cardly.security.UserPrincipal;
import com.cardly.service.DeckService;
import com.cardly.web.dto.DeckSearchRequest;
import com.cardly.web.dto.DeckResponse;
import com.cardly.web.dto.PagedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/community/subjects")
public class CommunityController {

	private final DeckService deckService;

	public CommunityController(DeckService deckService) {
		this.deckService = deckService;
	}

	@PostMapping("/search")
	public PagedResponse<DeckResponse> searchCommunitySubjects(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestBody(required = false) DeckSearchRequest request) {
		DeckSearchRequest safeRequest = request == null ? new DeckSearchRequest(null, null, true, null, 0, 20) : request;
		return deckService.searchCommunityDecks(principal.getId(), safeRequest);
	}

	@PostMapping("/{deckId}/clone")
	@ResponseStatus(HttpStatus.CREATED)
	public DeckResponse cloneCommunitySubject(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer deckId) {
		var sourceDeck = deckService.findPublicDeckById(deckId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (sourceDeck.getUser().getId().equals(principal.getId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot clone your own subject");
		}
		return deckService.cloneDeckToUser(sourceDeck, principal.getId());
	}
}
