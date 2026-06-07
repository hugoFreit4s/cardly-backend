package com.cardly.repository;

import com.cardly.domain.FriendRequest;
import com.cardly.domain.FriendRequestStatusENUM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {

	Optional<FriendRequest> findByRequester_IdAndReceiver_IdAndDeletedAtIsNull(Integer requesterId, Integer receiverId);

	Optional<FriendRequest> findByIdAndReceiver_IdAndDeletedAtIsNull(Integer requestId, Integer receiverId);

	Optional<FriendRequest> findByIdAndRequester_IdAndDeletedAtIsNull(Integer requestId, Integer requesterId);

	List<FriendRequest> findByReceiver_IdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
			Integer receiverId, FriendRequestStatusENUM status);

	List<FriendRequest> findByRequester_IdAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
			Integer requesterId, FriendRequestStatusENUM status);

	List<FriendRequest> findByRequester_IdAndStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(
			Integer requesterId, FriendRequestStatusENUM status);

	List<FriendRequest> findByReceiver_IdAndStatusAndDeletedAtIsNullOrderByUpdatedAtDesc(
			Integer receiverId, FriendRequestStatusENUM status);

	boolean existsByRequester_IdAndReceiver_IdAndStatusAndDeletedAtIsNull(
			Integer requesterId, Integer receiverId, FriendRequestStatusENUM status);
}
