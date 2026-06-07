package com.cardly.web.dto;

import jakarta.validation.constraints.NotNull;

public record SendFriendRequestRequest(
		@NotNull Integer receiverPublicId
) {
}
