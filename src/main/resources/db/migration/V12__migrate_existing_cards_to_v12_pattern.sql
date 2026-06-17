-- Normalize legacy scheduling data (v9-v11 behavior) to v12 rules.
-- Legacy correct answers were stored as HARD + DAYS_1 with increasing right_streak.
-- v12 equivalent:
--   right_streak = 1 -> MEDIUM + DAYS_1
--   right_streak >= 2 -> EASY + HOURS_36
-- For future-due EASY cards we add +12h to preserve the 36h cadence.

UPDATE cards
SET
	difficulty_level = CASE
		WHEN right_streak >= 2 THEN 'EASY'
		ELSE 'MEDIUM'
	END,
	scheduled_interval = CASE
		WHEN right_streak >= 2 THEN 'HOURS_36'
		ELSE 'DAYS_1'
	END,
	due_at = CASE
		WHEN right_streak >= 2
			AND due_at IS NOT NULL
			AND due_at > NOW()
		THEN due_at + INTERVAL '12 hours'
		ELSE due_at
	END
WHERE deleted_at IS NULL
	AND difficulty_level = 'HARD'
	AND scheduled_interval = 'DAYS_1'
	AND right_streak > 0;
