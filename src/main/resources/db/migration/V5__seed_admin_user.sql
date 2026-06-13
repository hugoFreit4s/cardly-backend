INSERT INTO users (name, email, password, role, created_at, updated_at)
SELECT 'Administrador', 'adm@cardly.com',
	'$2a$10$w3i7oTnhjqv2GVudkjwS2O4BZGKJ58muzeklhZ2lsASpYMpIxx1Lq',
	'SUPERADMIN', now(), now()
WHERE NOT EXISTS (
	SELECT 1 FROM users WHERE email = 'adm@cardly.com'
);
