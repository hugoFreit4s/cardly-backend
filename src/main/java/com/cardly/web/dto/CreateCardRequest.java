package com.cardly.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCardRequest(
		@NotBlank String question,
		@NotBlank String answer
) {
}
