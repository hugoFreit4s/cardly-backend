package com.cardly.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeckRequest(
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Size(max = 255) String subject,
		Boolean isPublic,
		Integer position
) {
}
