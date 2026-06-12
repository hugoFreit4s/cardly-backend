package com.cardly.specification;

import com.cardly.domain.User;
import com.cardly.domain.UserRoleENUM;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class UserSpecification {

	private UserSpecification() {
	}

	public static Specification<User> active() {
		return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
	}

	public static Specification<User> emailContains(String email) {
		if (!StringUtils.hasText(email)) {
			return Specification.where(null);
		}
		String value = "%" + email.trim().toLowerCase() + "%";
		return (root, query, cb) -> cb.like(cb.lower(root.get("email")), value);
	}

	public static Specification<User> nameContains(String name) {
		if (!StringUtils.hasText(name)) {
			return Specification.where(null);
		}
		String value = "%" + name.trim().toLowerCase() + "%";
		return (root, query, cb) -> cb.like(cb.lower(root.get("name")), value);
	}

	public static Specification<User> roleEquals(UserRoleENUM role) {
		if (role == null) {
			return Specification.where(null);
		}
		return (root, query, cb) -> cb.equal(root.get("role"), role);
	}

	public static Specification<User> excludeId(Integer userId) {
		if (userId == null) {
			return Specification.where(null);
		}
		return (root, query, cb) -> cb.notEqual(root.get("id"), userId);
	}
}
