package com.cardly.web.dto;

import java.util.List;

public record FriendProfileResponse(
		Integer publicId,
		String name,
		String email,
		long totalSubjects,
		long totalCards,
		long dueCards,
		List<DeckResponse> subjects
) {
}
