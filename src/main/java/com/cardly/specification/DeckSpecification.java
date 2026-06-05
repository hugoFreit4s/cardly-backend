package com.cardly.specification;

import com.cardly.domain.Deck;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class DeckSpecification {

	private DeckSpecification() {
	}

	public static Specification<Deck> active() {
		return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
	}

	public static Specification<Deck> byOwner(Integer userId) {
		if (userId == null) {
			return Specification.where(null);
		}
		return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
	}

	public static Specification<Deck> isPublic(Boolean isPublic) {
		if (isPublic == null) {
			return Specification.where(null);
		}
		return (root, query, cb) -> cb.equal(root.get("isPublic"), isPublic);
	}

	public static Specification<Deck> excludeOwner(Integer userId) {
		if (userId == null) {
			return Specification.where(null);
		}
		return (root, query, cb) -> cb.notEqual(root.get("user").get("id"), userId);
	}

	public static Specification<Deck> nameContains(String name) {
		if (!StringUtils.hasText(name)) {
			return Specification.where(null);
		}
		String value = "%" + name.trim().toLowerCase() + "%";
		return (root, query, cb) -> cb.like(cb.lower(root.get("name")), value);
	}

	public static Specification<Deck> subjectContains(String subject) {
		if (!StringUtils.hasText(subject)) {
			return Specification.where(null);
		}
		String value = "%" + subject.trim().toLowerCase() + "%";
		return (root, query, cb) -> cb.like(cb.lower(root.get("subject")), value);
	}
}
