-- Базовая таблица пользователя бота
CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    telegram_id BIGINT NOT NULL UNIQUE,
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,

    -- слоты под базовые поля онбординга (пока допускаем NULL)
    name TEXT,
    sex VARCHAR(8),
    age INTEGER,
    height_cm INTEGER,
    weight_kg NUMERIC(5,2),
    training_level VARCHAR(16),
    city TEXT,
    phone TEXT,
    geo_lat DOUBLE PRECISION,
    geo_lon DOUBLE PRECISION,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_app_user_telegram_id ON app_user(telegram_id);
