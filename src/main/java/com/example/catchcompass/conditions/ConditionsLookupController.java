package com.example.catchcompass.conditions;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Called by the catch form to prefill conditions before saving, so the angler
 * can review and correct the values rather than having them applied silently.
 */
@RestController
public class ConditionsLookupController {

    private final WeatherConditionsProvider weatherProvider;
    private final TideConditionsProvider tideProvider;

    public ConditionsLookupController(WeatherConditionsProvider weatherProvider,
                                      TideConditionsProvider tideProvider) {
        this.weatherProvider = weatherProvider;
        this.tideProvider = tideProvider;
    }

    @GetMapping("/api/conditions")
    public ResponseEntity<LookupResult> lookup(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant at) {

        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return ResponseEntity.badRequest().build();
        }

        WeatherConditions weather = weatherProvider
                .getConditions(latitude, longitude, at)
                .orElse(null);

        TideConditionsProvider.TideConditions tide = tideProvider
                .getConditions(latitude, longitude, at)
                .orElse(null);

        // 200 with nulls rather than 404: "we looked and found nothing" is a
        // different answer from "that endpoint does not exist", and the browser
        // handles them differently.
        return ResponseEntity.ok(new LookupResult(
                weather == null ? null : weather.airTemperatureC(),
                weather == null ? null : weather.windSpeedMetersPerSecond(),
                weather == null ? null : weather.windDirectionDegrees(),
                weather == null ? null : weather.barometricPressureHpa(),
                weather == null || weather.skyCondition() == null ? null : weather.skyCondition().name(),
                weather == null ? null : weather.observedAt(),
                tide == null ? null : tide.tideHeightMeters(),
                tide == null || tide.tideState() == null ? null : tide.tideState().name(),
                weather != null));
    }

    public record LookupResult(
            BigDecimal airTemperatureC,
            BigDecimal windSpeedMetersPerSecond,
            Integer windDirectionDegrees,
            BigDecimal barometricPressureHpa,
            String skyCondition,
            Instant observedAt,
            BigDecimal tideHeightMeters,
            String tideState,
            boolean weatherAvailable) {
    }
}
