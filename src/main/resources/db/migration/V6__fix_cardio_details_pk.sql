-- Добавляем surrogate PK в cardio_workout_details и делаем workout_id уникальным

ALTER TABLE cardio_workout_details
    DROP CONSTRAINT cardio_workout_details_pkey;

ALTER TABLE cardio_workout_details
    ADD COLUMN id BIGSERIAL PRIMARY KEY;

ALTER TABLE cardio_workout_details
    ADD CONSTRAINT uq_cardio_workout_details_workout_id UNIQUE (workout_id);
