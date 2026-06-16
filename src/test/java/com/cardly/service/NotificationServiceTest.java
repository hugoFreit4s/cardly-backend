package com.cardly.service;

import com.cardly.domain.Notification;
import com.cardly.domain.NotificationTypeENUM;
import com.cardly.domain.User;
import com.cardly.repository.CardRepository;
import com.cardly.repository.NotificationRepository;
import com.cardly.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CardRepository cardRepository;

	@InjectMocks
	private NotificationService notificationService;

	@Test
	void notifyReviewExpiredSkipsWhenReferenceExists() {
		User user = new User();
		user.setId(3);
		when(userRepository.findByIdAndDeletedAtIsNull(3)).thenReturn(Optional.of(user));
		when(notificationRepository.existsByUser_IdAndReferenceKeyAndDeletedAtIsNull(eq(3), any())).thenReturn(true);

		notificationService.notifyReviewExpiredForUser(3, 5);

		verify(notificationRepository, never()).save(any(Notification.class));
	}

	@Test
	void notifyReviewExpiredCreatesNotification() {
		User user = new User();
		user.setId(3);
		when(userRepository.findByIdAndDeletedAtIsNull(3)).thenReturn(Optional.of(user));
		when(notificationRepository.existsByUser_IdAndReferenceKeyAndDeletedAtIsNull(eq(3), any())).thenReturn(false);
		when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

		notificationService.notifyReviewExpiredForUser(3, 2);

		ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
		verify(notificationRepository).save(captor.capture());
		Notification saved = captor.getValue();
		assertThat(saved.getType()).isEqualTo(NotificationTypeENUM.REVIEW_EXPIRED);
		assertThat(saved.getMessage()).contains("2 cartões vencidos");
	}

	@Test
	void scanExpiredReviewsNotifiesUsersWithDueCards() {
		when(cardRepository.findUserIdsWithDueCardsOnly(any())).thenReturn(List.of(7));
		when(cardRepository.countByDeck_User_IdAndDeletedAtIsNullAndDueAtLessThanEqual(eq(7), any())).thenReturn(1L);
		User user = new User();
		user.setId(7);
		when(userRepository.findByIdAndDeletedAtIsNull(7)).thenReturn(Optional.of(user));
		when(notificationRepository.existsByUser_IdAndReferenceKeyAndDeletedAtIsNull(eq(7), any())).thenReturn(false);
		when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

		notificationService.scanExpiredReviewsAndNotify();

		verify(notificationRepository).save(any(Notification.class));
	}
}
