package com.example.catchcompass.conditions;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface TideConditionsProvider {

    Optional<TideConditions> getConditions(double latitude, double longitude, Instant caughtAt);

    record TideConditions(BigDecimal tideHeightMeters, TideState tideState, Instant observedAt) {
    }
}
