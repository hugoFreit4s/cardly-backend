package com.cardly.web.dto;

import java.time.Instant;

public record AuthResponse(String token, Instant expiresAt) {
}
