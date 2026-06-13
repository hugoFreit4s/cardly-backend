package com.cardly.web.dto;

public record AuthConfigResponse(
		boolean googleAuthEnabled,
		String googleWebClientId
) {
}
