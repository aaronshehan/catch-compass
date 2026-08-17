package com.example.catchcompass.conditions;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OpenMeteoWeatherProviderTest {

    /**
     * Port 1 has nothing listening, so this is a connection refused in a few
     * milliseconds. Your README requires that provider failures never stop
     * someone recording a catch, which means this must return empty, not throw.
     */
    @Test
    void returnsEmptyRatherThanThrowingWhenTheProviderIsUnreachable() {
        OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider(new WeatherProperties(
                "http://127.0.0.1:1/forecast",
                "http://127.0.0.1:1/archive",
                Duration.ofMillis(250),
                Duration.ofMillis(250)));

        assertThat(provider.getConditions(44.5, -73.2, Instant.now())).isEmpty();
    }

    @Test
    void tideProviderReportsNoDataRatherThanFailing() {
        TideConditionsProvider provider = new UnavailableTideConditionsProvider();

        assertThat(provider.getConditions(44.5, -73.2, Instant.now())).isEmpty();
    }
}
