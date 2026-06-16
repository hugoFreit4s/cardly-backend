package com.cardly.web;

import com.cardly.domain.User;
import com.cardly.security.UserPrincipal;
import com.cardly.service.CardService;
import com.cardly.service.DeckService;
import com.cardly.service.FriendRequestService;
import com.cardly.web.dto.FriendProfileResponse;
import com.cardly.web.dto.FriendSummaryResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendNetworkController {

	private final FriendRequestService friendRequestService;
	private final DeckService deckService;
	private final CardService cardService;

	public FriendNetworkController(
			FriendRequestService friendRequestService,
			DeckService deckService,
			CardService cardService) {
		this.friendRequestService = friendRequestService;
		this.deckService = deckService;
		this.cardService = cardService;
	}

	@GetMapping
	public List<FriendSummaryResponse> listFriends(@AuthenticationPrincipal UserPrincipal principal) {
		return friendRequestService.listFriends(principal.getId());
	}

	@GetMapping("/{friendPublicId}/profile")
	public FriendProfileResponse getFriendProfile(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer friendPublicId) {
		User friend = friendRequestService.requireFriend(principal.getId(), friendPublicId);
		return new FriendProfileResponse(
				friend.getPublicId(),
				friend.getName(),
				friend.getEmail(),
				deckService.countSubjects(friend.getId()),
				cardService.countCards(friend.getId()),
				cardService.countDueCards(friend.getId()),
				deckService.listDecks(friend.getId())
		);
	}
}
