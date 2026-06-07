package com.cardly.service;

import com.cardly.domain.FriendRequest;
import com.cardly.domain.FriendRequestStatusENUM;
import com.cardly.domain.User;
import com.cardly.domain.UserRoleENUM;
import com.cardly.repository.FriendRequestRepository;
import com.cardly.repository.UserRepository;
import com.cardly.web.dto.FriendRequestResponse;
import com.cardly.web.dto.FriendSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendRequestServiceTest {

	@Mock
	private FriendRequestRepository friendRequestRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private FriendRequestService friendRequestService;

	@Test
	void sendRequestResolvesReceiverByPublicId() {
		User requester = new User();
		requester.setId(1);
		requester.setPublicId(100001);
		requester.setEmail("a@b.com");
		requester.setRole(UserRoleENUM.USER);

		User receiver = new User();
		receiver.setId(2);
		receiver.setPublicId(100002);
		receiver.setEmail("c@d.com");
		receiver.setRole(UserRoleENUM.USER);

		when(userRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(requester));
		when(userRepository.findByPublicIdAndDeletedAtIsNull(100002)).thenReturn(Optional.of(receiver));
		when(friendRequestRepository.findByRequester_IdAndReceiver_IdAndDeletedAtIsNull(1, 2))
				.thenReturn(Optional.empty());
		when(friendRequestRepository.save(any(FriendRequest.class))).thenAnswer(invocation -> {
			FriendRequest request = invocation.getArgument(0);
			request.setId(10);
			return request;
		});

		FriendRequestResponse response = friendRequestService.sendRequest(1, 100002);

		assertThat(response.receiverPublicId()).isEqualTo(100002);
		assertThat(response.requesterPublicId()).isEqualTo(100001);
	}

	@Test
	void sendRequestRejectsSelfByPublicId() {
		User requester = new User();
		requester.setId(1);
		requester.setPublicId(100001);
		requester.setEmail("a@b.com");

		when(userRepository.findByIdAndDeletedAtIsNull(1)).thenReturn(Optional.of(requester));
		when(userRepository.findByPublicIdAndDeletedAtIsNull(100001)).thenReturn(Optional.of(requester));

		assertThatThrownBy(() -> friendRequestService.sendRequest(1, 100001))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void listFriendsReturnsAcceptedFriendsFromBothSides() {
		User me = user(1, 100001, "me@cardly.com", "Me");
		User anna = user(2, 100002, "anna@cardly.com", "Anna");
		User bruno = user(3, 100003, "bruno@cardly.com", "Bruno");

		FriendRequest sentAccepted = new FriendRequest();
		sentAccepted.setRequester(me);
		sentAccepted.setReceiver(anna);
		sentAccepted.setStatus(FriendRequestStatusENUM.ACCEPTED);

		FriendRequest receivedAccepted = new FriendRequest();
		receivedAccepted.setRequester(bruno);
		receivedAccepted.setReceiver(me);
		receivedAccepted.setStatus(FriendRequestStatusENUM.ACCEPTED);

		when(friendRequestRepository.findByRequester_IdAndStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(1, FriendRequestStatusENUM.ACCEPTED))
				.thenReturn(List.of(sentAccepted));
		when(friendRequestRepository.findByReceiver_IdAndStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(1, FriendRequestStatusENUM.ACCEPTED))
				.thenReturn(List.of(receivedAccepted));

		List<FriendSummaryResponse> friends = friendRequestService.listFriends(1);

		assertThat(friends).hasSize(2);
		assertThat(friends).extracting(FriendSummaryResponse::publicId).containsExactly(100002, 100003);
	}

	@Test
	void requireFriendRejectsWhenUsersAreNotFriends() {
		User target = user(2, 100002, "target@cardly.com", "Target");

		when(userRepository.findByPublicIdAndDeletedAtIsNull(100002)).thenReturn(Optional.of(target));
		when(friendRequestRepository.existsByRequester_IdAndReceiver_IdAndStatusAndDeletedAtIsNull(1, 2, FriendRequestStatusENUM.ACCEPTED))
				.thenReturn(false);
		when(friendRequestRepository.existsByRequester_IdAndReceiver_IdAndStatusAndDeletedAtIsNull(2, 1, FriendRequestStatusENUM.ACCEPTED))
				.thenReturn(false);

		assertThatThrownBy(() -> friendRequestService.requireFriend(1, 100002))
				.isInstanceOf(ResponseStatusException.class)
				.extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
				.isEqualTo(HttpStatus.FORBIDDEN);
	}

	private User user(int id, int publicId, String email, String name) {
		User user = new User();
		user.setId(id);
		user.setPublicId(publicId);
		user.setEmail(email);
		user.setName(name);
		user.setRole(UserRoleENUM.USER);
		return user;
	}
}
