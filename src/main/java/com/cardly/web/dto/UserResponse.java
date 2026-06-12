package com.cardly.web.dto;

import com.cardly.domain.UserRoleENUM;

import java.time.Instant;

public record UserResponse(
		Integer id,
		Integer publicId,
		String name,
		String email,
		UserRoleENUM role,
		Instant createdAt,
		Instant updatedAt
) {
}
