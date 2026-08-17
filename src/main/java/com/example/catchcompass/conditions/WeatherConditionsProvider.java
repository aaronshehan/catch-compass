package com.example.catchcompass.conditions;

import java.time.Instant;
import java.util.Optional;

/**
 * Weather for a catch, behind an interface so the rest of the application never
 * knows or cares which service supplied it.
 */
public interface WeatherConditionsProvider {

    /**
     * Returns empty rather than throwing when the provider is unreachable, slow,
     * or has no data for that place and time. A weather outage must never stop
     * someone recording a catch.
     */
    Optional<WeatherConditions> getConditions(double latitude, double longitude, Instant caughtAt);
}
