-- One row per catch. catch_id is both the primary key and the foreign key,
-- which is how the database enforces "at most one conditions record per catch".
CREATE TABLE catch_conditions (
    catch_id                     BIGINT PRIMARY KEY
                                     REFERENCES catches (id) ON DELETE CASCADE,

    air_temperature_c            NUMERIC(4, 1),
    water_temperature_c          NUMERIC(4, 1),

    wind_speed_meters_per_second NUMERIC(5, 2),
    wind_direction_degrees       INTEGER,

    tide_height_meters           NUMERIC(5, 2),
    tide_state                   VARCHAR(20),

    barometric_pressure_hpa      NUMERIC(6, 1),
    sky_condition                VARCHAR(20),

    conditions_source            VARCHAR(20)  NOT NULL,
    observed_at                  TIMESTAMPTZ,

    CONSTRAINT catch_conditions_air_temperature_range
        CHECK (air_temperature_c BETWEEN -60 AND 60),
    CONSTRAINT catch_conditions_water_temperature_range
        CHECK (water_temperature_c BETWEEN -5 AND 45),
    CONSTRAINT catch_conditions_wind_speed_range
        CHECK (wind_speed_meters_per_second BETWEEN 0 AND 120),

    -- Meteorological convention: the direction the wind comes FROM.
    CONSTRAINT catch_conditions_wind_direction_range
        CHECK (wind_direction_degrees BETWEEN 0 AND 359),

    CONSTRAINT catch_conditions_tide_height_range
        CHECK (tide_height_meters BETWEEN -5 AND 20),
    CONSTRAINT catch_conditions_pressure_range
        CHECK (barometric_pressure_hpa BETWEEN 800 AND 1100),

    CONSTRAINT catch_conditions_tide_state_valid
        CHECK (tide_state IN ('HIGH', 'LOW', 'RISING', 'FALLING',
                              'UNKNOWN', 'NOT_APPLICABLE')),
    CONSTRAINT catch_conditions_sky_condition_valid
        CHECK (sky_condition IN ('CLEAR', 'PARTLY_CLOUDY', 'CLOUDY', 'OVERCAST',
                                 'RAIN', 'SNOW', 'FOG')),
    CONSTRAINT catch_conditions_source_valid
        CHECK (conditions_source IN ('MANUAL', 'WEATHER_API', 'WEATHER_API_EDITED'))
);
