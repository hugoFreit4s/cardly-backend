package com.cardly.web.dto;

import com.cardly.domain.NotificationTypeENUM;

import java.time.Instant;

public record NotificationResponse(
		Integer id,
		NotificationTypeENUM type,
		String title,
		String message,
		Instant readAt,
		boolean read,
		Instant createdAt
) {
}
