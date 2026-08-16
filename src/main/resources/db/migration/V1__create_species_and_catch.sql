CREATE TABLE species (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    common_name     VARCHAR(100) NOT NULL UNIQUE,
    scientific_name VARCHAR(150),
    water_type      VARCHAR(20)  NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT species_water_type_valid
        CHECK (water_type IN ('FRESHWATER', 'SALTWATER', 'BRACKISH'))
);

INSERT INTO species (common_name, scientific_name, water_type) VALUES
    ('Largemouth Bass',  'Micropterus nigricans', 'FRESHWATER'),
    ('Northern Pike',    'Esox lucius',           'FRESHWATER'),
    ('Rainbow Trout',    'Oncorhynchus mykiss',   'FRESHWATER'),
    ('Bluegill',         'Lepomis macrochirus',   'FRESHWATER'),
    ('Striped Bass',     'Morone saxatilis',      'BRACKISH'),
    ('Atlantic Cod',     'Gadus morhua',          'SALTWATER');

CREATE TABLE catches (
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id                  BIGINT      NOT NULL,
    species_id               BIGINT      NOT NULL REFERENCES species (id),
    caught_at                TIMESTAMPTZ NOT NULL,

    latitude                 NUMERIC(8, 6),
    longitude                NUMERIC(9, 6),
    location_accuracy_meters NUMERIC(8, 2),

    weight_kg                NUMERIC(6, 3),
    length_cm                NUMERIC(6, 2),
    circumference_cm         NUMERIC(6, 2),

    notes                    TEXT,

    created_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT catches_latitude_range
        CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT catches_longitude_range
        CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT catches_location_paired
        CHECK ((latitude IS NULL) = (longitude IS NULL)),
    CONSTRAINT catches_accuracy_positive
        CHECK (location_accuracy_meters > 0),
    CONSTRAINT catches_weight_positive
        CHECK (weight_kg > 0),
    CONSTRAINT catches_length_positive
        CHECK (length_cm > 0),
    CONSTRAINT catches_circumference_positive
        CHECK (circumference_cm > 0)
);

CREATE INDEX catches_user_caught_at_idx ON catches (user_id, caught_at DESC);
CREATE INDEX catches_species_idx        ON catches (species_id);