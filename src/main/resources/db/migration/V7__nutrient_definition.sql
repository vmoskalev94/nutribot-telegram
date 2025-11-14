CREATE TABLE IF NOT EXISTS nutrient_definition (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(32)  NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    unit VARCHAR(32)  NOT NULL
);

INSERT INTO nutrient_definition (code, name, unit) VALUES
    ('MG', 'Магний', 'mg'),
    ('D3', 'Витамин D3', 'IU')
ON CONFLICT (code) DO NOTHING;
