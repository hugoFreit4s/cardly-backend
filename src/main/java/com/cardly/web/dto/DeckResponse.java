package com.cardly.web.dto;

import java.time.Instant;

public record DeckResponse(
		Integer id,
		String name,
		Integer position,
		String subject,
		boolean isPublic,
		int cardCount,
		int scheduledCardCount,
		int waitingCardCount,
		int readyRevisionCount,
		String ownerName,
		Instant createdAt,
		Instant updatedAt,
		boolean alreadyCloned,
		Integer clonedDeckId
) {
}
