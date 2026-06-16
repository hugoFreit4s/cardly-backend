package com.cardly.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

	private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

	private final NotificationService notificationService;

	public NotificationScheduler(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@Scheduled(fixedDelayString = "${cardly.notifications.review-scan-ms:300000}")
	public void scanExpiredReviews() {
		try {
			notificationService.scanExpiredReviewsAndNotify();
		} catch (Exception ex) {
			log.warn("Failed to scan expired reviews for notifications", ex);
		}
	}
}
