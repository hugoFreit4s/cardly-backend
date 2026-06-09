package com.cardly.web.dto;

public record DashboardResponse(
		long totalSubjects,
		long totalCards,
		long dueCards,
		long answeredToday
) {
}
