CREATE TABLE app_user (
    id            UUID       PRIMARY KEY,
    email         TEXT       NOT NULL UNIQUE,
    password_hash TEXT       NOT NULL,
    role          TEXT       NOT NULL,
    person_id     UUID       REFERENCES person(id),
    created_at    TIMESTAMP  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_user_email ON app_user(email);
