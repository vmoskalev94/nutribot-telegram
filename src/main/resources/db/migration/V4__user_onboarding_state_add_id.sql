-- Меняем первичный ключ таблицы user_onboarding_state:
--   добавляем surrogate PK id, а user_id делаем просто уникальным внешним ключом.

ALTER TABLE user_onboarding_state
    DROP CONSTRAINT user_onboarding_state_pkey;

ALTER TABLE user_onboarding_state
    ADD COLUMN id BIGSERIAL PRIMARY KEY;

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_onboarding_state_user_id
    ON user_onboarding_state(user_id);
