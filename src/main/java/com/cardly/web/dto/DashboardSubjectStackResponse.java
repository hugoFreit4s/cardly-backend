package com.cardly.web.dto;

public record DashboardSubjectStackResponse(
		String subject,
		long totalCards,
		long dueCards,
		long scheduledCards,
		long unscheduledCards
) {
}
