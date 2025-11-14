-- Состояние онбординга по пользователю
CREATE TABLE IF NOT EXISTS user_onboarding_state (
    user_id     BIGINT PRIMARY KEY REFERENCES app_user(id) ON DELETE CASCADE,
    current_step VARCHAR(32) NOT NULL,
    data_json   TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
