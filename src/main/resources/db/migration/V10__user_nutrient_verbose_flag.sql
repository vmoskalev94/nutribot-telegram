ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS nutrient_verbose boolean NOT NULL DEFAULT FALSE;
