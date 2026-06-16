CREATE TABLE notifications (
	id SERIAL PRIMARY KEY,
	user_id INTEGER NOT NULL REFERENCES users(id),
	type VARCHAR(64) NOT NULL,
	title VARCHAR(255) NOT NULL,
	message TEXT NOT NULL,
	read_at TIMESTAMPTZ,
	reference_key VARCHAR(255),
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL,
	deleted_at TIMESTAMPTZ,
	CONSTRAINT chk_notifications_type CHECK (
		type IN ('REVIEW_EXPIRED', 'FRIEND_REQUEST_RECEIVED', 'FRIEND_REQUEST_ACCEPTED')
	)
);

CREATE UNIQUE INDEX uq_notifications_user_reference
	ON notifications (user_id, reference_key)
	WHERE deleted_at IS NULL AND reference_key IS NOT NULL;

CREATE INDEX idx_notifications_user_created
	ON notifications (user_id, created_at DESC);

CREATE INDEX idx_notifications_user_unread
	ON notifications (user_id)
	WHERE deleted_at IS NULL AND read_at IS NULL;
