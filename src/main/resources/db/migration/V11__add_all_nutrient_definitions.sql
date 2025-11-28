-- Добавляем все нутриенты в справочник

INSERT INTO nutrient_definition (code, name, unit) VALUES
    ('FE', 'Железо', 'mg'),
    ('ZN', 'Цинк', 'mg'),
    ('OMEGA3', 'Омега-3', 'mg'),
    ('B6', 'Витамин B6', 'mg'),
    ('CA', 'Кальций', 'mg'),
    ('C', 'Витамин C', 'mg'),
    ('B12', 'Витамин B12', 'mcg'),
    ('SE', 'Селен', 'mcg'),
    ('CU', 'Медь', 'mg'),
    ('E', 'Витамин E', 'mg'),
    ('I', 'Йод', 'mcg'),
    ('K', 'Витамин K', 'mcg'),
    ('B9', 'Фолат (B9)', 'mcg')
ON CONFLICT (code) DO NOTHING;
