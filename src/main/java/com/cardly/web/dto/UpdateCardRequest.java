package com.cardly.web.dto;

public record UpdateCardRequest(
		String question,
		String answer
) {
}
