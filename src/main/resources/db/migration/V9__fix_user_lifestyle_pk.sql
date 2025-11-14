-- Меняем PK в user_lifestyle с user_id на surrogate id

ALTER TABLE user_lifestyle
    DROP CONSTRAINT user_lifestyle_pkey;

ALTER TABLE user_lifestyle
    ADD COLUMN id BIGSERIAL PRIMARY KEY;

ALTER TABLE user_lifestyle
    ADD CONSTRAINT uq_user_lifestyle_user_id UNIQUE (user_id);
