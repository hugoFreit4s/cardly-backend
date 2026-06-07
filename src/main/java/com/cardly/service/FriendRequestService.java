package com.cardly.service;

import com.cardly.domain.FriendRequest;
import com.cardly.domain.FriendRequestStatusENUM;
import com.cardly.domain.User;
import com.cardly.repository.FriendRequestRepository;
import com.cardly.repository.UserRepository;
import com.cardly.web.dto.FriendRequestResponse;
import com.cardly.web.dto.FriendSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class FriendRequestService {

	private final FriendRequestRepository friendRequestRepository;
	private final UserRepository userRepository;
	private final NotificationService notificationService;

	public FriendRequestService(
			FriendRequestRepository friendRequestRepository,
			UserRepository userRepository,
			NotificationService notificationService) {
		this.friendRequestRepository = friendRequestRepository;
		this.userRepository = userRepository;
		this.notificationService = notificationService;
	}

	@Transactional
	public FriendRequestResponse sendRequest(Integer requesterId, Integer receiverPublicId) {
		User requester = userRepository.findByIdAndDeletedAtIsNull(requesterId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester not found"));
		User receiver = userRepository.findByPublicIdAndDeletedAtIsNull(receiverPublicId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found"));
		if (requester.getPublicId().equals(receiver.getPublicId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot send a friend request to yourself");
		}

		FriendRequest request = friendRequestRepository
				.findByRequester_IdAndReceiver_IdAndDeletedAtIsNull(requesterId, receiver.getId())
				.orElseGet(FriendRequest::new);
		if (request.getId() != null && request.getStatus() == FriendRequestStatusENUM.PENDING) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A pending request already exists");
		}
		request.setRequester(requester);
		request.setReceiver(receiver);
		request.setStatus(FriendRequestStatusENUM.PENDING);
		request.setDeletedAt(null);
		FriendRequest saved = friendRequestRepository.save(request);
		notificationService.notifyFriendRequestReceived(saved);
		return toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<FriendRequestResponse> listReceivedPending(Integer userId) {
		return friendRequestRepository
				.findByReceiver_IdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(userId, FriendRequestStatusENUM.PENDING)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<FriendRequestResponse> listSentPending(Integer userId) {
		return friendRequestRepository
				.findByRequester_IdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(userId, FriendRequestStatusENUM.PENDING)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<FriendSummaryResponse> listFriends(Integer userId) {
		List<FriendRequest> acceptedSent = friendRequestRepository
				.findByRequester_IdAndStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(userId, FriendRequestStatusENUM.ACCEPTED);
		List<FriendRequest> acceptedReceived = friendRequestRepository
				.findByReceiver_IdAndStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(userId, FriendRequestStatusENUM.ACCEPTED);

		Map<Integer, FriendSummaryResponse> friendsByPublicId = new LinkedHashMap<>();
		Stream.concat(acceptedSent.stream(), acceptedReceived.stream())
				.filter(request -> request.getRequester() != null && request.getReceiver() != null)
				.sorted(Comparator.comparing(FriendRequest::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
						.reversed())
				.forEach(request -> {
					User friend = request.getRequester().getId().equals(userId) ? request.getReceiver() : request.getRequester();
					if (friend.getDeletedAt() != null || friendsByPublicId.containsKey(friend.getPublicId())) {
						return;
					}
					friendsByPublicId.put(friend.getPublicId(), new FriendSummaryResponse(
							friend.getPublicId(),
							friend.getName(),
							friend.getEmail(),
							request.getUpdatedAt()
					));
				});
		return friendsByPublicId.values().stream()
				.sorted(Comparator.comparing(FriendSummaryResponse::name, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	@Transactional(readOnly = true)
	public User requireFriend(Integer userId, Integer friendPublicId) {
		User friend = userRepository.findByPublicIdAndDeletedAtIsNull(friendPublicId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend not found"));
		if (friend.getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use your own profile endpoint");
		}
		boolean accepted = friendRequestRepository.existsByRequester_IdAndReceiver_IdAndStatusAndDeletedAtIsNull(
				userId, friend.getId(), FriendRequestStatusENUM.ACCEPTED)
				|| friendRequestRepository.existsByRequester_IdAndReceiver_IdAndStatusAndDeletedAtIsNull(
				friend.getId(), userId, FriendRequestStatusENUM.ACCEPTED);
		if (!accepted) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Users are not friends");
		}
		return friend;
	}

	@Transactional
	public FriendRequestResponse accept(Integer userId, Integer requestId) {
		FriendRequest request = friendRequestRepository.findByIdAndReceiver_IdAndDeletedAtIsNull(requestId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));
		if (request.getStatus() != FriendRequestStatusENUM.PENDING) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending requests can be accepted");
		}
		request.setStatus(FriendRequestStatusENUM.ACCEPTED);
		FriendRequest saved = friendRequestRepository.save(request);
		notificationService.notifyFriendRequestAccepted(saved);
		return toResponse(saved);
	}

	@Transactional
	public FriendRequestResponse deny(Integer userId, Integer requestId) {
		FriendRequest request = friendRequestRepository.findByIdAndReceiver_IdAndDeletedAtIsNull(requestId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));
		if (request.getStatus() != FriendRequestStatusENUM.PENDING) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending requests can be denied");
		}
		request.setStatus(FriendRequestStatusENUM.DENIED);
		return toResponse(friendRequestRepository.save(request));
	}

	@Transactional
	public void unsend(Integer userId, Integer requestId) {
		FriendRequest request = friendRequestRepository.findByIdAndRequester_IdAndDeletedAtIsNull(requestId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));
		if (request.getStatus() != FriendRequestStatusENUM.PENDING) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending requests can be canceled");
		}
		request.setStatus(FriendRequestStatusENUM.CANCELLED);
		request.setDeletedAt(Instant.now());
		friendRequestRepository.save(request);
	}

	private FriendRequestResponse toResponse(FriendRequest request) {
		return new FriendRequestResponse(
				request.getId(),
				request.getRequester().getPublicId(),
				request.getRequester().getEmail(),
				request.getReceiver().getPublicId(),
				request.getReceiver().getEmail(),
				request.getStatus(),
				request.getCreatedAt(),
				request.getUpdatedAt()
		);
	}
}
