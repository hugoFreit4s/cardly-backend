package com.cardly.web;

import com.cardly.security.UserPrincipal;
import com.cardly.service.FriendRequestService;
import com.cardly.web.dto.FriendRequestResponse;
import com.cardly.web.dto.SendFriendRequestRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/friends/requests")
public class FriendController {

	private final FriendRequestService friendRequestService;

	public FriendController(FriendRequestService friendRequestService) {
		this.friendRequestService = friendRequestService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FriendRequestResponse sendFriendRequest(
			@AuthenticationPrincipal UserPrincipal principal,
			@Valid @RequestBody SendFriendRequestRequest request) {
		return friendRequestService.sendRequest(principal.getId(), request.receiverPublicId());
	}

	@GetMapping("/received")
	public List<FriendRequestResponse> listReceivedPending(@AuthenticationPrincipal UserPrincipal principal) {
		return friendRequestService.listReceivedPending(principal.getId());
	}

	@GetMapping("/sent")
	public List<FriendRequestResponse> listSentPending(@AuthenticationPrincipal UserPrincipal principal) {
		return friendRequestService.listSentPending(principal.getId());
	}

	@PostMapping("/{requestId}/accept")
	public FriendRequestResponse acceptRequest(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer requestId) {
		return friendRequestService.accept(principal.getId(), requestId);
	}

	@PostMapping("/{requestId}/deny")
	public FriendRequestResponse denyRequest(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer requestId) {
		return friendRequestService.deny(principal.getId(), requestId);
	}

	@DeleteMapping("/{requestId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void unsendRequest(
			@AuthenticationPrincipal UserPrincipal principal,
			@PathVariable Integer requestId) {
		friendRequestService.unsend(principal.getId(), requestId);
	}
}
