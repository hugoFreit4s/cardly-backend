package com.cardly.service;

import com.cardly.domain.FriendRequest;
import com.cardly.domain.Notification;
import com.cardly.domain.NotificationTypeENUM;
import com.cardly.domain.User;
import com.cardly.repository.CardRepository;
import com.cardly.repository.NotificationRepository;
import com.cardly.repository.UserRepository;
import com.cardly.web.dto.NotificationResponse;
import com.cardly.web.dto.NotificationSummaryResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;
	private final CardRepository cardRepository;

	public NotificationService(
			NotificationRepository notificationRepository,
			UserRepository userRepository,
			CardRepository cardRepository) {
		this.notificationRepository = notificationRepository;
		this.userRepository = userRepository;
		this.cardRepository = cardRepository;
	}

	@Transactional(readOnly = true)
	public List<NotificationResponse> listForUser(Integer userId) {
		return notificationRepository.findByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public NotificationSummaryResponse summaryForUser(Integer userId) {
		long unread = notificationRepository.countByUser_IdAndDeletedAtIsNullAndReadAtIsNull(userId);
		return new NotificationSummaryResponse(unread);
	}

	@Transactional
	public void markAllRead(Integer userId) {
		notificationRepository.markAllReadForUser(userId, Instant.now());
	}

	@Transactional
	public void markRead(Integer userId, List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		notificationRepository.markReadByIds(userId, ids, Instant.now());
	}

	@Transactional
	public void deleteNotifications(Integer userId, List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		int deleted = notificationRepository.softDeleteByIds(userId, ids, Instant.now());
		if (deleted == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notifications not found");
		}
	}

	@Transactional
	public void notifyFriendRequestReceived(FriendRequest request) {
		User receiver = request.getReceiver();
		User requester = request.getRequester();
		String referenceKey = "friend-request-received:" + request.getId();
		createIfAbsent(
				receiver,
				NotificationTypeENUM.FRIEND_REQUEST_RECEIVED,
				"Solicitação de amizade",
				requester.getName() + " enviou uma solicitação de amizade.",
				referenceKey);
	}

	@Transactional
	public void notifyFriendRequestAccepted(FriendRequest request) {
		User requester = request.getRequester();
		User receiver = request.getReceiver();
		String referenceKey = "friend-request-accepted:" + request.getId();
		createIfAbsent(
				requester,
				NotificationTypeENUM.FRIEND_REQUEST_ACCEPTED,
				"Solicitação aceita",
				receiver.getName() + " aceitou sua solicitação de amizade.",
				referenceKey);
	}

	@Transactional
	public void notifyReviewExpiredForUser(Integer userId, long dueCount) {
		if (dueCount <= 0) {
			return;
		}
		User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
		if (user == null) {
			return;
		}
		String day = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC).format(Instant.now());
		String referenceKey = "review-expired:" + userId + ":" + day;
		String message = dueCount == 1
				? "Você tem 1 cartão vencido para revisar."
				: "Você tem " + dueCount + " cartões vencidos para revisar.";
		createIfAbsent(
				user,
				NotificationTypeENUM.REVIEW_EXPIRED,
				"Revisão vencida",
				message,
				referenceKey);
	}

	@Transactional
	public void scanExpiredReviewsAndNotify() {
		Instant now = Instant.now();
		List<Integer> userIds = cardRepository.findUserIdsWithDueCardsOnly(now);
		for (Integer userId : userIds) {
			long dueCount = cardRepository.countByDeck_User_IdAndDeletedAtIsNullAndDueAtLessThanEqual(userId, now);
			notifyReviewExpiredForUser(userId, dueCount);
		}
	}

	private void createIfAbsent(
			User user,
			NotificationTypeENUM type,
			String title,
			String message,
			String referenceKey) {
		if (notificationRepository.existsByUser_IdAndReferenceKeyAndDeletedAtIsNull(user.getId(), referenceKey)) {
			return;
		}
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setType(type);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setReferenceKey(referenceKey);
		try {
			notificationRepository.save(notification);
		} catch (DataIntegrityViolationException ignored) {
		}
	}

	private NotificationResponse toResponse(Notification notification) {
		return new NotificationResponse(
				notification.getId(),
				notification.getType(),
				notification.getTitle(),
				notification.getMessage(),
				notification.getReadAt(),
				notification.getReadAt() != null,
				notification.getCreatedAt()
		);
	}
}
