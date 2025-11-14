-- Initial technical migration for Nutribot
CREATE TABLE IF NOT EXISTS flyway_init_marker (
    id SMALLINT PRIMARY KEY DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);