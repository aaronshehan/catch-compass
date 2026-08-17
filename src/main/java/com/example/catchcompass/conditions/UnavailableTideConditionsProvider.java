package com.example.catchcompass.conditions;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * Open-Meteo does not publish tide predictions, so there is nothing to call yet.
 *
 * <p>This exists rather than leaving the interface unimplemented so the rest of
 * the application can already depend on {@link TideConditionsProvider} and treat
 * "no tide data" as the ordinary case it will always be on inland water.
 * Swapping in a real provider means adding one class and deleting this one.
 */
@Component
public class UnavailableTideConditionsProvider implements TideConditionsProvider {

    @Override
    public Optional<TideConditions> getConditions(double latitude, double longitude, Instant caughtAt) {
        return Optional.empty();
    }
}
