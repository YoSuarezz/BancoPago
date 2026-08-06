CREATE TABLE app_user (
    id            UUID          PRIMARY KEY,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    role          VARCHAR(20)   NOT NULL,
    person_id     UUID          REFERENCES person(id),
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_user_email ON app_user(email);
