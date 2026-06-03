CREATE TABLE users (
	id SERIAL PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	email VARCHAR(320) NOT NULL,
	password VARCHAR(255) NOT NULL,
	role VARCHAR(32) NOT NULL,
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL,
	deleted_at TIMESTAMPTZ,
	CONSTRAINT uq_users_email UNIQUE (email),
	CONSTRAINT chk_users_role CHECK (role IN ('USER', 'SUPERADMIN'))
);

CREATE TABLE decks (
	id SERIAL PRIMARY KEY,
	user_id INTEGER NOT NULL REFERENCES users (id),
	name VARCHAR(255) NOT NULL,
	position INTEGER NOT NULL,
	subject VARCHAR(255) NOT NULL,
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL,
	deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_decks_user_id ON decks (user_id);

CREATE TABLE cards (
	id SERIAL PRIMARY KEY,
	deck_id INTEGER NOT NULL REFERENCES decks (id),
	question TEXT NOT NULL,
	answer TEXT NOT NULL,
	difficulty_level VARCHAR(32) NOT NULL,
	due_at TIMESTAMPTZ,
	right_streak INTEGER NOT NULL,
	wrong_streak INTEGER NOT NULL,
	scheduled_interval VARCHAR(32),
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL,
	deleted_at TIMESTAMPTZ,
	CONSTRAINT chk_cards_difficulty CHECK (difficulty_level IN ('NONE', 'EASY', 'MEDIUM', 'HARD')),
	CONSTRAINT chk_cards_scheduled_interval CHECK (
		scheduled_interval IS NULL
		OR scheduled_interval IN ('DAYS_1', 'DAYS_2', 'DAYS_3', 'DAYS_5', 'DAYS_7', 'DAYS_9')
	)
);

CREATE INDEX idx_cards_deck_id ON cards (deck_id);
