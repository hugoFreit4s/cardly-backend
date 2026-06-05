package com.cardly.web.dto;

import com.cardly.domain.DifficultyLevelENUM;
import com.cardly.domain.ScheduledIntervalENUM;

import java.time.Instant;

public record CardResponse(
		Integer id,
		Integer deckId,
		String question,
		String answer,
		DifficultyLevelENUM difficultyLevel,
		Instant dueAt,
		int rightStreak,
		int wrongStreak,
		ScheduledIntervalENUM scheduledInterval,
		Instant createdAt,
		Instant updatedAt
) {
}
