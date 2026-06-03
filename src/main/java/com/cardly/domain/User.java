package com.cardly.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, length = 320)
	private String email;

	@Column(name = "password", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private UserRoleENUM role;

	@Column(name = "public_id", nullable = false, unique = true)
	private Integer publicId;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<Deck> decks = new ArrayList<>();

	public User() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public UserRoleENUM getRole() {
		return role;
	}

	public void setRole(UserRoleENUM role) {
		this.role = role;
	}

	public Integer getPublicId() {
		return publicId;
	}

	public void setPublicId(Integer publicId) {
		this.publicId = publicId;
	}

	public List<Deck> getDecks() {
		return decks;
	}

	public void setDecks(List<Deck> decks) {
		this.decks = decks;
	}
}
