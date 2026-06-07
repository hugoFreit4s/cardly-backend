CREATE TABLE friend_requests (
	id SERIAL PRIMARY KEY,
	requester_id INTEGER NOT NULL REFERENCES users (id),
	receiver_id INTEGER NOT NULL REFERENCES users (id),
	status VARCHAR(32) NOT NULL,
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL,
	deleted_at TIMESTAMPTZ,
	CONSTRAINT chk_friend_request_status CHECK (status IN ('PENDING', 'ACCEPTED', 'DENIED', 'CANCELLED')),
	CONSTRAINT chk_friend_request_different_users CHECK (requester_id <> receiver_id),
	CONSTRAINT uq_friend_request_pair UNIQUE (requester_id, receiver_id)
);

CREATE INDEX idx_friend_requests_requester ON friend_requests (requester_id);
CREATE INDEX idx_friend_requests_receiver ON friend_requests (receiver_id);

CREATE TABLE study_review_events (
	id SERIAL PRIMARY KEY,
	user_id INTEGER NOT NULL REFERENCES users (id),
	deck_id INTEGER NOT NULL REFERENCES decks (id),
	card_id INTEGER NOT NULL REFERENCES cards (id),
	result VARCHAR(32) NOT NULL,
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL,
	deleted_at TIMESTAMPTZ,
	CONSTRAINT chk_review_event_result CHECK (result IN ('CORRECT', 'WRONG', 'SKIPPED'))
);

CREATE INDEX idx_study_review_events_user ON study_review_events (user_id, created_at DESC);
CREATE INDEX idx_study_review_events_card ON study_review_events (card_id);
