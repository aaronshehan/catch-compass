package com.example.catchcompass.catchlog;

import com.example.catchcompass.conditions.CatchConditions;
import com.example.catchcompass.lure.CatchLureSnapshot;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The JSON shape of a catch.
 *
 * <p>Entities are deliberately never serialised directly. They carry lazy
 * associations that blow up outside a transaction, they expose {@code userId},
 * and every future field would silently join the public API. A DTO makes the
 * contract something you choose rather than something you leak.
 */
public final class CatchResponse {

    private CatchResponse() {
    }

    public record Summary(
            Long id,
            String species,
            Instant caughtAt,
            BigDecimal weightKg,
            BigDecimal lengthCm,
            boolean hasPhoto,
            String photoUrl) {

        public static Summary from(Catch source, boolean hasPhoto) {
            return new Summary(
                    source.getId(),
                    source.getSpecies().getCommonName(),
                    source.getCaughtAt(),
                    source.getWeightKg(),
                    source.getLengthCm(),
                    hasPhoto,
                    hasPhoto ? "/api/catches/" + source.getId() + "/photo" : null);
        }
    }

    public record Detail(
            Long id,
            SpeciesRef species,
            Instant caughtAt,
            Location location,
            Measurements measurements,
            String notes,
            boolean hasPhoto,
            String photoUrl,
            LureView lure,
            ConditionsView conditions,
            Instant createdAt,
            Instant updatedAt) {

        public static Detail from(Catch source,
                                  boolean hasPhoto,
                                  CatchLureSnapshot lure,
                                  CatchConditions conditions) {
            return new Detail(
                    source.getId(),
                    new SpeciesRef(source.getSpecies().getId(), source.getSpecies().getCommonName(),
                            source.getSpecies().getScientificName()),
                    source.getCaughtAt(),
                    source.getLatitude() == null ? null : new Location(
                            source.getLatitude(),
                            source.getLongitude(),
                            source.getLocationAccuracyMeters(),
                            source.getLocationRecordedAt()),
                    new Measurements(source.getWeightKg(), source.getLengthCm(),
                            source.getCircumferenceCm()),
                    source.getNotes(),
                    hasPhoto,
                    hasPhoto ? "/api/catches/" + source.getId() + "/photo" : null,
                    LureView.from(lure),
                    ConditionsView.from(conditions),
                    source.getCreatedAt(),
                    source.getUpdatedAt());
        }
    }

    public record SpeciesRef(Long id, String commonName, String scientificName) {
    }

    public record Location(
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracyMeters,
            Instant recordedAt) {
    }

    public record Measurements(BigDecimal weightKg, BigDecimal lengthCm, BigDecimal circumferenceCm) {
    }

    public record LureView(
            Long lureId,
            String displayName,
            String type,
            String size,
            BigDecimal weightGrams,
            String presentation,
            boolean stillInTackleBox) {

        static LureView from(CatchLureSnapshot snapshot) {
            if (snapshot == null) {
                return null;
            }
            return new LureView(
                    snapshot.getLureId(),
                    snapshot.getDisplayName(),
                    snapshot.getLureType().name(),
                    snapshot.getSize(),
                    snapshot.getWeightGrams(),
                    snapshot.getPresentation() == null ? null : snapshot.getPresentation().name(),
                    snapshot.getLureId() != null);
        }
    }

    public record ConditionsView(
            BigDecimal airTemperatureC,
            BigDecimal waterTemperatureC,
            BigDecimal windSpeedMetersPerSecond,
            Integer windDirectionDegrees,
            String windDirectionLabel,
            BigDecimal tideHeightMeters,
            String tideState,
            BigDecimal barometricPressureHpa,
            String skyCondition,
            String source,
            Instant observedAt) {

        static ConditionsView from(CatchConditions conditions) {
            if (conditions == null) {
                return null;
            }
            return new ConditionsView(
                    conditions.getAirTemperatureC(),
                    conditions.getWaterTemperatureC(),
                    conditions.getWindSpeedMetersPerSecond(),
                    conditions.getWindDirectionDegrees(),
                    conditions.getWindDirectionLabel(),
                    conditions.getTideHeightMeters(),
                    conditions.getTideState() == null ? null : conditions.getTideState().name(),
                    conditions.getBarometricPressureHpa(),
                    conditions.getSkyCondition() == null ? null : conditions.getSkyCondition().name(),
                    conditions.getConditionsSource().name(),
                    conditions.getObservedAt());
        }
    }
}
