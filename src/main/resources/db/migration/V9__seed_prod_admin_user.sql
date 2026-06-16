INSERT INTO users (name, email, password, role, public_id, created_at, updated_at)
SELECT 'Administrador', 'administrador@cardly.com',
	'$2a$10$BsD0ueTbrWeZxNghBJ2BKeeVRyVsebIrtsYswCgb5m6IrR7AztQuS',
	'SUPERADMIN', nextval('users_public_id_seq'), now(), now()
WHERE NOT EXISTS (
	SELECT 1 FROM users WHERE email = 'administrador@cardly.com'
);
