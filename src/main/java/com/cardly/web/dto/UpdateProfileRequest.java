package com.cardly.web.dto;

public record UpdateProfileRequest(
		String name,
		String currentPassword,
		String newPassword
) {
}
