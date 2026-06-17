ALTER TABLE cards DROP CONSTRAINT chk_cards_scheduled_interval;

ALTER TABLE cards ADD CONSTRAINT chk_cards_scheduled_interval CHECK (
	scheduled_interval IS NULL
	OR scheduled_interval IN ('HOURS_2', 'HOURS_4', 'HOURS_36', 'DAYS_1', 'DAYS_2', 'DAYS_3', 'DAYS_5', 'DAYS_7', 'DAYS_9')
);
