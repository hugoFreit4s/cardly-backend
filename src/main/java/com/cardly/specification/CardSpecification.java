package com.cardly.specification;

import com.cardly.domain.Card;
import com.cardly.domain.DifficultyLevelENUM;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;

public final class CardSpecification {

	private CardSpecification() {
	}

	public static Specification<Card> active() {
		return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
	}

	public static Specification<Card> byDeck(Integer deckId) {
		if (deckId == null) {
			return Specification.where(null);
		}
		return (root, query, cb) -> cb.equal(root.get("deck").get("id"), deckId);
	}

	public static Specification<Card> byOwner(Integer userId) {
		if (userId == null) {
			return Specification.where(null);
		}
		return (root, query, cb) -> cb.equal(root.get("deck").get("user").get("id"), userId);
	}

	public static Specification<Card> questionContains(String question) {
		if (!StringUtils.hasText(question)) {
			return Specification.where(null);
		}
		String value = "%" + question.trim().toLowerCase() + "%";
		return (root, query, cb) -> cb.like(cb.lower(root.get("question")), value);
	}

	public static Specification<Card> difficultyEquals(DifficultyLevelENUM difficultyLevel) {
		if (difficultyLevel == null) {
			return Specification.where(null);
		}
		return (root, query, cb) -> cb.equal(root.get("difficultyLevel"), difficultyLevel);
	}

	public static Specification<Card> dueNow(Boolean dueOnly) {
		if (!Boolean.TRUE.equals(dueOnly)) {
			return Specification.where(null);
		}
		Instant now = Instant.now();
		return (root, query, cb) -> cb.or(
				cb.isNull(root.get("dueAt")),
				cb.lessThanOrEqualTo(root.get("dueAt"), now));
	}

	public static Specification<Card> scheduledOnly(Boolean scheduledOnly) {
		if (!Boolean.TRUE.equals(scheduledOnly)) {
			return Specification.where(null);
		}
		return (root, query, cb) -> cb.isNotNull(root.get("dueAt"));
	}

	public static Specification<Card> waitingOnly(Boolean waitingOnly) {
		if (!Boolean.TRUE.equals(waitingOnly)) {
			return Specification.where(null);
		}
		Instant now = Instant.now();
		return (root, query, cb) -> cb.greaterThan(root.get("dueAt"), now);
	}

	public static Specification<Card> readyScheduledOnly(Boolean readyScheduledOnly) {
		if (!Boolean.TRUE.equals(readyScheduledOnly)) {
			return Specification.where(null);
		}
		Instant now = Instant.now();
		return (root, query, cb) -> cb.and(
				cb.isNotNull(root.get("dueAt")),
				cb.lessThanOrEqualTo(root.get("dueAt"), now));
	}
}
