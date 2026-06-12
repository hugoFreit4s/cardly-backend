package com.cardly.web.dto;

import com.cardly.domain.UserRoleENUM;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateUserRequest(
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Email @Size(max = 320) String email,
		@NotBlank @Size(min = 8, max = 128) String password,
		UserRoleENUM role
) {
}
