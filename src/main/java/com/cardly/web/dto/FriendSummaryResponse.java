package com.cardly.web.dto;

import java.time.Instant;

public record FriendSummaryResponse(
		Integer publicId,
		String name,
		String email,
		Instant friendsSince
) {
}
