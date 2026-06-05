package com.cardly.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNormalizerTest {

	@Test
	void normalizeTrimsAndLowercases() {
		assertThat(EmailNormalizer.normalize("  User@Example.COM  ")).isEqualTo("user@example.com");
	}

	@Test
	void normalizeNullReturnsNull() {
		assertThat(EmailNormalizer.normalize(null)).isNull();
	}
}