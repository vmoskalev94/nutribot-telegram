-- Базовая таблица тренировок
CREATE TABLE IF NOT EXISTS workout (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES app_user(id),
    type        VARCHAR(16) NOT NULL,         -- STRENGTH / CARDIO
    started_at  TIMESTAMPTZ NOT NULL,
    status      VARCHAR(16) NOT NULL DEFAULT 'DRAFT'  -- DRAFT / ACTIVE / DELETED
);

CREATE INDEX IF NOT EXISTS idx_workout_user_started_at
    ON workout(user_id, started_at DESC);


-- Силовая тренировка

CREATE TABLE IF NOT EXISTS strength_exercise (
    id           BIGSERIAL PRIMARY KEY,
    workout_id   BIGINT      NOT NULL REFERENCES workout(id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    order_index  INT         NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_strength_exercise_workout_order
    ON strength_exercise(workout_id, order_index);


CREATE TABLE IF NOT EXISTS strength_set (
    id           BIGSERIAL PRIMARY KEY,
    exercise_id  BIGINT      NOT NULL REFERENCES strength_exercise(id) ON DELETE CASCADE,
    weight       NUMERIC(10,2) NOT NULL,
    reps         INT         NOT NULL,
    rir          INT,
    order_index  INT         NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_strength_set_exercise_order
    ON strength_set(exercise_id, order_index);


-- Кардио-тренировка

CREATE TABLE IF NOT EXISTS cardio_workout_details (
    workout_id    BIGINT      PRIMARY KEY REFERENCES workout(id) ON DELETE CASCADE,
    activity_type VARCHAR(64) NOT NULL,      -- бег, велосипед и т.п.
    duration_min  INT         NOT NULL,
    distance_km   NUMERIC(10,2),
    intensity     VARCHAR(16),               -- LOW / MODERATE / HIGH
    rpe           INT                         -- если пользователь ввёл RPE числом
);
