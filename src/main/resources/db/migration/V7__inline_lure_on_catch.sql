-- The tackle box is gone. A lure is no longer a reusable entity that catches
-- point at, so there is nothing to snapshot: the details simply belong to the
-- catch. Two columns is not worth a table of its own.

ALTER TABLE catches
    ADD COLUMN lure_type        VARCHAR(40),
    ADD COLUMN lure_description VARCHAR(200);

ALTER TABLE catches
    ADD CONSTRAINT catches_lure_type_valid
        CHECK (lure_type IN ('CRANKBAIT', 'SPINNERBAIT', 'JIG', 'SOFT_PLASTIC',
                             'TOPWATER', 'SPOON', 'FLY', 'LIVE_BAIT',
                             'CUT_BAIT', 'OTHER'));

-- A description of nothing is meaningless.
ALTER TABLE catches
    ADD CONSTRAINT catches_lure_description_needs_type
        CHECK (lure_description IS NULL OR lure_type IS NOT NULL);

-- Carry across whatever the snapshots already recorded, folding the separate
-- brand/model/colour columns into the single description field.
UPDATE catches
SET lure_type = snapshot.lure_type,
    lure_description = NULLIF(
        trim(concat_ws(' ', snapshot.brand, snapshot.model, snapshot.color)), '')
FROM catch_lure_snapshots snapshot
WHERE snapshot.catch_id = catches.id;

DROP TABLE catch_lure_snapshots;
DROP TABLE lures;
