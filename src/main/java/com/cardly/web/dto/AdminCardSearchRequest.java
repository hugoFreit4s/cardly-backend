package com.cardly.web.dto;

import com.cardly.domain.DifficultyLevelENUM;

public record AdminCardSearchRequest(
		Integer ownerId,
		Integer deckId,
		String question,
		DifficultyLevelENUM difficultyLevel,
		Boolean dueOnly,
		Integer page,
		Integer size
) {
}
