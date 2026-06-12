package com.cardly.web.dto;

import com.cardly.domain.UserRoleENUM;

public record UpdateUserRequest(
		String name,
		UserRoleENUM role
) {
}
