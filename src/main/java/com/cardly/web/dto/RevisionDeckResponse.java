package com.cardly.web.dto;

import java.util.List;

public record RevisionDeckResponse(
		Integer id,
		String name,
		String subject,
		int scheduledCardCount,
		int waitingCardCount,
		int readyRevisionCount,
		List<CardResponse> cards
) {
}
