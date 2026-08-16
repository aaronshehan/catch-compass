ALTER TABLE catches
    ADD COLUMN location_recorded_at TIMESTAMPTZ;

-- A location reading time is meaningless without a location.
ALTER TABLE catches
    ADD CONSTRAINT catches_location_time_requires_coordinates
        CHECK (location_recorded_at IS NULL OR latitude IS NOT NULL);
