INSERT INTO decks (user_id, name, position, subject, is_public, created_at, updated_at)
SELECT u.id, v.name, v.position, v.subject, TRUE, now(), now()
FROM users u
CROSS JOIN (
	VALUES
		('Matemática Essencial', 1, 'Matemática'),
		('Geografia do Brasil', 2, 'Geografia'),
		('Português: Gramática', 3, 'Português'),
		('Inglês Básico', 4, 'Inglês'),
		('Ciências Naturais', 5, 'Ciências'),
		('História Geral', 6, 'História')
) AS v(name, position, subject)
WHERE u.email = 'adm@cardly.com'
	AND NOT EXISTS (
		SELECT 1 FROM decks d
		WHERE d.user_id = u.id AND d.name = v.name
	);

INSERT INTO cards (deck_id, question, answer, difficulty_level, right_streak, wrong_streak, created_at, updated_at)
SELECT d.id, c.question, c.answer, 'NONE', 0, 0, now(), now()
FROM decks d
JOIN users u ON u.id = d.user_id
CROSS JOIN (
	VALUES
		('Matemática Essencial', 'Quanto é 7 x 8?', '56'),
		('Matemática Essencial', 'Qual é a raiz quadrada de 81?', '9'),
		('Matemática Essencial', 'Quanto é 15% de 200?', '30'),
		('Matemática Essencial', 'Qual a soma dos ângulos internos de um triângulo?', '180 graus'),
		('Matemática Essencial', 'Quanto é 12 ao quadrado?', '144'),

		('Geografia do Brasil', 'Qual é a capital do Brasil?', 'Brasília'),
		('Geografia do Brasil', 'Qual é o maior estado brasileiro em área?', 'Amazonas'),
		('Geografia do Brasil', 'Qual bioma cobre a maior parte do Norte do Brasil?', 'Amazônia'),
		('Geografia do Brasil', 'Qual é o rio mais extenso do Brasil?', 'Rio Amazonas'),
		('Geografia do Brasil', 'Quantas regiões geográficas o Brasil possui?', 'Cinco'),

		('Português: Gramática', 'Qual é o plural de "cidadão"?', 'Cidadãos'),
		('Português: Gramática', 'O que é um substantivo?', 'Palavra que nomeia seres, objetos e conceitos'),
		('Português: Gramática', 'Qual a função do adjetivo?', 'Caracterizar o substantivo'),
		('Português: Gramática', 'O que é um verbo?', 'Palavra que indica ação, estado ou fenômeno'),
		('Português: Gramática', 'Qual o antônimo de "rápido"?', 'Lento'),

		('Inglês Básico', 'Como se diz "casa" em inglês?', 'House'),
		('Inglês Básico', 'Qual o passado do verbo "go"?', 'Went'),
		('Inglês Básico', 'Traduza: "good morning".', 'Bom dia'),
		('Inglês Básico', 'Como se diz "obrigado" em inglês?', 'Thank you'),
		('Inglês Básico', 'Qual o plural de "child"?', 'Children'),

		('Ciências Naturais', 'Qual é o gás que as plantas absorvem na fotossíntese?', 'Gás carbônico (CO2)'),
		('Ciências Naturais', 'Qual planeta é conhecido como Planeta Vermelho?', 'Marte'),
		('Ciências Naturais', 'Qual é a unidade básica da vida?', 'A célula'),
		('Ciências Naturais', 'Qual órgão bombeia o sangue no corpo humano?', 'O coração'),
		('Ciências Naturais', 'Qual é o estado físico da água a 0 graus Celsius?', 'Sólido (gelo)'),

		('História Geral', 'Em que ano o Brasil foi descoberto pelos portugueses?', '1500'),
		('História Geral', 'Quem proclamou a independência do Brasil?', 'Dom Pedro I'),
		('História Geral', 'Em que ano começou a Segunda Guerra Mundial?', '1939'),
		('História Geral', 'Qual civilização construiu as pirâmides de Gizé?', 'Os egípcios'),
		('História Geral', 'Em que ano caiu o Muro de Berlim?', '1989')
) AS c(deck_name, question, answer)
WHERE u.email = 'adm@cardly.com'
	AND d.name = c.deck_name
	AND NOT EXISTS (
		SELECT 1 FROM cards existing
		WHERE existing.deck_id = d.id AND existing.question = c.question
	);
