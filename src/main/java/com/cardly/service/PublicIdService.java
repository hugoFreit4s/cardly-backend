package com.cardly.service;

import com.cardly.domain.User;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicIdService {

	private final EntityManager entityManager;

	public PublicIdService(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Transactional
	public void ensurePublicId(User user) {
		if (user.getPublicId() != null) {
			return;
		}
		user.setPublicId(nextPublicId());
	}

	private int nextPublicId() {
		Number value = (Number) entityManager
				.createNativeQuery("SELECT nextval('users_public_id_seq')")
				.getSingleResult();
		return value.intValue();
	}
}
