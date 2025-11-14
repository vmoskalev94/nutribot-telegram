create TABLE IF NOT EXISTS user_lifestyle (
    user_id              BIGINT       PRIMARY KEY
        REFERENCES app_user(id) ON delete CASCADE,
    smoke_packs_per_day  NUMERIC(4,2),
    vegan                BOOLEAN,
    pregnant             BOOLEAN
);
