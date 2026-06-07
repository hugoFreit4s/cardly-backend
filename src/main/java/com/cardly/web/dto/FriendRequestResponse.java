package com.cardly.web.dto;

import com.cardly.domain.FriendRequestStatusENUM;

import java.time.Instant;

public record FriendRequestResponse(
		Integer id,
		Integer requesterPublicId,
		String requesterEmail,
		Integer receiverPublicId,
		String receiverEmail,
		FriendRequestStatusENUM status,
		Instant createdAt,
		Instant updatedAt
) {
}
