package com.cardly.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "decks")
public class Deck extends BaseEntity {

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer position;

	@Column(nullable = false)
	private String subject;

	@Column(name = "is_public", nullable = false)
	private boolean isPublic;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_deck_id")
	private Deck sourceDeck;

	@OneToMany(mappedBy = "deck", fetch = FetchType.LAZY)
	private List<Card> cards = new ArrayList<>();

	public Deck() {
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean aPublic) {
		isPublic = aPublic;
	}

	public Deck getSourceDeck() {
		return sourceDeck;
	}

	public void setSourceDeck(Deck sourceDeck) {
		this.sourceDeck = sourceDeck;
	}

	public List<Card> getCards() {
		return cards;
	}

	public void setCards(List<Card> cards) {
		this.cards = cards;
	}
}
