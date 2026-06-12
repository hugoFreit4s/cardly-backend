package com.cardly.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCreateDeckRequest(
		@NotNull Integer ownerId,
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Size(max = 255) String subject,
		Boolean isPublic,
		Integer position
) {
}
