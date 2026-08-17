CREATE TABLE lures (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT       NOT NULL,

    lure_type    VARCHAR(40)  NOT NULL,
    brand        VARCHAR(100),
    model        VARCHAR(100),
    color        VARCHAR(60),
    size         VARCHAR(40),
    weight_grams NUMERIC(7, 2),
    presentation VARCHAR(40),
    notes        TEXT,

    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT lures_weight_positive
        CHECK (weight_grams > 0),
    CONSTRAINT lures_type_valid
        CHECK (lure_type IN ('CRANKBAIT', 'SPINNERBAIT', 'JIG', 'SOFT_PLASTIC',
                             'TOPWATER', 'SPOON', 'FLY', 'LIVE_BAIT',
                             'CUT_BAIT', 'OTHER')),
    CONSTRAINT lures_presentation_valid
        CHECK (presentation IN ('CASTING', 'TROLLING', 'JIGGING', 'DRIFTING',
                                'STILL_FISHING', 'FLY_CASTING', 'OTHER'))
);

CREATE INDEX lures_user_idx ON lures (user_id, lure_type);

-- A frozen copy of the lure as it was when the catch was recorded.
--
-- lure_id is nullable and ON DELETE SET NULL on purpose: deleting a lure from
-- the tackle box must not destroy the record of what caught a fish two years
-- ago. The copied columns are the historical truth; the reference is only a
-- convenience for "show me other catches on this lure".
CREATE TABLE catch_lure_snapshots (
    catch_id     BIGINT PRIMARY KEY
                     REFERENCES catches (id) ON DELETE CASCADE,
    lure_id      BIGINT
                     REFERENCES lures (id) ON DELETE SET NULL,

    lure_type    VARCHAR(40)  NOT NULL,
    brand        VARCHAR(100),
    model        VARCHAR(100),
    color        VARCHAR(60),
    size         VARCHAR(40),
    weight_grams NUMERIC(7, 2),
    presentation VARCHAR(40),

    captured_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT catch_lure_snapshots_type_valid
        CHECK (lure_type IN ('CRANKBAIT', 'SPINNERBAIT', 'JIG', 'SOFT_PLASTIC',
                             'TOPWATER', 'SPOON', 'FLY', 'LIVE_BAIT',
                             'CUT_BAIT', 'OTHER')),
    CONSTRAINT catch_lure_snapshots_presentation_valid
        CHECK (presentation IN ('CASTING', 'TROLLING', 'JIGGING', 'DRIFTING',
                                'STILL_FISHING', 'FLY_CASTING', 'OTHER'))
);

CREATE INDEX catch_lure_snapshots_lure_idx ON catch_lure_snapshots (lure_id);
