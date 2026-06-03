package com.cardly.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "cards")
public class Card extends BaseEntity {

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "deck_id", nullable = false)
	private Deck deck;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String question;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String answer;

	@Enumerated(EnumType.STRING)
	@Column(name = "difficulty_level", nullable = false, length = 32)
	private DifficultyLevelENUM difficultyLevel = DifficultyLevelENUM.NONE;

	@Column(name = "due_at")
	private Instant dueAt;

	@Column(name = "right_streak", nullable = false)
	private int rightStreak;

	@Column(name = "wrong_streak", nullable = false)
	private int wrongStreak;

	@Enumerated(EnumType.STRING)
	@Column(name = "scheduled_interval", length = 32)
	private ScheduledIntervalENUM scheduledInterval;

	public Card() {
	}

	public Deck getDeck() {
		return deck;
	}

	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public DifficultyLevelENUM getDifficultyLevel() {
		return difficultyLevel;
	}

	public void setDifficultyLevel(DifficultyLevelENUM difficultyLevel) {
		this.difficultyLevel = difficultyLevel;
	}

	public Instant getDueAt() {
		return dueAt;
	}

	public void setDueAt(Instant dueAt) {
		this.dueAt = dueAt;
	}

	public int getRightStreak() {
		return rightStreak;
	}

	public void setRightStreak(int rightStreak) {
		this.rightStreak = rightStreak;
	}

	public int getWrongStreak() {
		return wrongStreak;
	}

	public void setWrongStreak(int wrongStreak) {
		this.wrongStreak = wrongStreak;
	}

	public ScheduledIntervalENUM getScheduledInterval() {
		return scheduledInterval;
	}

	public void setScheduledInterval(ScheduledIntervalENUM scheduledInterval) {
		this.scheduledInterval = scheduledInterval;
	}
}
