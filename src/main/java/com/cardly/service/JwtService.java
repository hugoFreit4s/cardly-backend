package com.cardly.service;

import com.cardly.config.JwtProperties;
import com.cardly.domain.User;
import com.cardly.domain.UserRoleENUM;
import com.cardly.web.dto.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

	private final JwtProperties jwtProperties;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
	}

	public AuthResponse buildAuthResponse(User user) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(jwtProperties.getExpirationSeconds());
		String token = createToken(user, now, exp);
		return new AuthResponse(token, exp);
	}

	public String createToken(User user, Instant issuedAt, Instant expiresAt) {
		return Jwts.builder()
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.claim("role", user.getRole().name())
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(expiresAt))
				.signWith(signingKey())
				.compact();
	}

	public ParsedJwt parse(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(signingKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		Integer userId = Integer.valueOf(claims.getSubject());
		String email = claims.get("email", String.class);
		UserRoleENUM role = UserRoleENUM.valueOf(claims.get("role", String.class));
		return new ParsedJwt(userId, email, role);
	}

	private SecretKey signingKey() {
		byte[] bytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			try {
				bytes = MessageDigest.getInstance("SHA-256").digest(bytes);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException(e);
			}
		}
		return Keys.hmacShaKeyFor(bytes);
	}

	public record ParsedJwt(Integer userId, String email, UserRoleENUM role) {
	}
}
