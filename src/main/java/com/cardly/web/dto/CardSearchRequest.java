package com.cardly.web.dto;

import com.cardly.domain.DifficultyLevelENUM;

public record CardSearchRequest(
		String question,
		DifficultyLevelENUM difficultyLevel,
		Boolean dueOnly,
		Boolean scheduledOnly,
		Boolean waitingOnly,
		Boolean readyScheduledOnly,
		Integer page,
		Integer size
) {
}
