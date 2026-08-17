CREATE TABLE users (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,

    -- Never the password. BCrypt hashes are 60 characters; the column is wider
    -- so the algorithm can be changed later without another migration.
    password_hash   VARCHAR(100) NOT NULL,

    unit_preference VARCHAR(20)  NOT NULL DEFAULT 'METRIC',
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT users_username_length
        CHECK (length(username) BETWEEN 3 AND 50),

    -- Stored lowercase so "Aaron" and "aaron" cannot both be registered.
    -- UNIQUE alone is case-sensitive and would allow exactly that.
    CONSTRAINT users_username_lowercase
        CHECK (username = lower(username)),

    CONSTRAINT users_unit_preference_valid
        CHECK (unit_preference IN ('METRIC', 'US_CUSTOMARY'))
);

-- Existing catches and lures were written against the hardcoded dev user id 1.
-- This table is new and empty, so the first insert takes id 1 and those rows
-- stay valid. The hash is deliberately not a valid BCrypt string, so no
-- password can ever match it and nobody can sign in as this account.
INSERT INTO users (username, password_hash, enabled)
VALUES ('legacy-dev-user', 'LOCKED-NOT-A-VALID-HASH', false);

-- Now the foreign keys the schema has been missing since V1.
ALTER TABLE catches
    ADD CONSTRAINT catches_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE lures
    ADD CONSTRAINT lures_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
