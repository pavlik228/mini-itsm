CREATE TABLE ticket (
    id          UUID          PRIMARY KEY,
    title       VARCHAR(200)  NOT NULL,
    description VARCHAR(4000) NULL,
    status      VARCHAR(32)   NOT NULL CHECK (status IN ('NEW')),
    created_at  TIMESTAMPTZ   NOT NULL
);
