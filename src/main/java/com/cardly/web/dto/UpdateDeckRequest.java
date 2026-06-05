package com.cardly.web.dto;

import jakarta.validation.constraints.Size;

public record UpdateDeckRequest(
		@Size(max = 255) String name,
		@Size(max = 255) String subject,
		Boolean isPublic,
		Integer position
) {
}
