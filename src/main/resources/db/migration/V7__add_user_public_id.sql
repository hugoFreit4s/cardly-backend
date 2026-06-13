ALTER TABLE users ADD COLUMN public_id INTEGER;

WITH numbered AS (
	SELECT id, 100000 + ROW_NUMBER() OVER (ORDER BY id) - 1 AS new_public_id
	FROM users
)
UPDATE users u
SET public_id = numbered.new_public_id
FROM numbered
WHERE u.id = numbered.id;

ALTER TABLE users ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT uq_users_public_id UNIQUE (public_id);

CREATE SEQUENCE users_public_id_seq;
SELECT setval('users_public_id_seq', (SELECT MAX(public_id) FROM users));
