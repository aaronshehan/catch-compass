CREATE TABLE catch_photos (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    catch_id     BIGINT       NOT NULL REFERENCES catches (id) ON DELETE CASCADE,
    storage_key  VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    size_bytes   BIGINT       NOT NULL,
    width_px     INTEGER      NOT NULL,
    height_px    INTEGER      NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT catch_photos_size_positive
        CHECK (size_bytes > 0),
    CONSTRAINT catch_photos_dimensions_positive
        CHECK (width_px > 0 AND height_px > 0),
    CONSTRAINT catch_photos_content_type_valid
        CHECK (content_type IN ('image/jpeg', 'image/png'))
);

CREATE INDEX catch_photos_catch_idx ON catch_photos (catch_id);
