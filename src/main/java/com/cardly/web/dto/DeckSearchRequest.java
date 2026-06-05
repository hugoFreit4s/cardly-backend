package com.cardly.web.dto;

public record DeckSearchRequest(
		String name,
		String subject,
		Boolean isPublic,
		Integer ownerId,
		Integer page,
		Integer size
) {
}
