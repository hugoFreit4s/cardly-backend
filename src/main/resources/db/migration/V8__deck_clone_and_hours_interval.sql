ALTER TABLE decks ADD COLUMN source_deck_id INTEGER REFERENCES decks(id);

CREATE UNIQUE INDEX uq_decks_user_source_deck
	ON decks (user_id, source_deck_id)
	WHERE deleted_at IS NULL AND source_deck_id IS NOT NULL;

ALTER TABLE cards DROP CONSTRAINT chk_cards_scheduled_interval;

ALTER TABLE cards ADD CONSTRAINT chk_cards_scheduled_interval CHECK (
	scheduled_interval IS NULL
	OR scheduled_interval IN ('HOURS_2', 'DAYS_1', 'DAYS_2', 'DAYS_3', 'DAYS_5', 'DAYS_7', 'DAYS_9')
);
