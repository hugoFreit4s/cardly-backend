package com.cardly.web.dto;

import com.cardly.domain.UserRoleENUM;

public record MeResponse(Integer publicId, String name, String email, UserRoleENUM role) {
}
