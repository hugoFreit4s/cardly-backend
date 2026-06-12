package com.cardly.web.dto;

import com.cardly.domain.UserRoleENUM;

public record UserSearchRequest(
		String name,
		String email,
		UserRoleENUM role,
		Integer page,
		Integer size
) {
}
