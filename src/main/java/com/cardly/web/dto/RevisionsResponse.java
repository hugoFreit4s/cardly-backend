package com.cardly.web.dto;

import java.util.List;

public record RevisionsResponse(
		List<RevisionDeckResponse> decks
) {
}
