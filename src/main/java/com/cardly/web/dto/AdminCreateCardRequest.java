package com.cardly.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminCreateCardRequest(
		@NotNull Integer deckId,
		@NotBlank String question,
		@NotBlank String answer
) {
}
