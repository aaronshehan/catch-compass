package com.example.catchcompass.conditions;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Weather at a place and time, as reported by some provider.
 *
 * <p>Every field is nullable: a provider that knows the temperature but not the
 * pressure should return what it has rather than nothing at all.
 */
public record WeatherConditions(
        BigDecimal airTemperatureC,
        BigDecimal windSpeedMetersPerSecond,
        Integer windDirectionDegrees,
        BigDecimal barometricPressureHpa,
        SkyCondition skyCondition,
        Instant observedAt) {
}
