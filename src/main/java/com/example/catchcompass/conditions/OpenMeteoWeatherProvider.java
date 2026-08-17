package com.example.catchcompass.conditions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Weather from Open-Meteo, which needs no API key.
 *
 * <p>Two endpoints: the forecast API covers roughly the last three months plus
 * the coming week, and the archive API covers older dates. Which one to use is
 * decided by how old the catch is.
 */
@Component
public class OpenMeteoWeatherProvider implements WeatherConditionsProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoWeatherProvider.class);

    private static final String HOURLY_FIELDS =
            "temperature_2m,wind_speed_10m,wind_direction_10m,surface_pressure,cloud_cover,precipitation";

    /** Beyond this age the forecast endpoint no longer carries the date. */
    private static final Duration ARCHIVE_THRESHOLD = Duration.ofDays(60);

    private final RestClient restClient;
    private final WeatherProperties properties;

    public OpenMeteoWeatherProvider(WeatherProperties properties) {
        this.properties = properties;

        // Timeouts are the whole ballgame here. Without them a slow provider
        // would hang the request thread, and logging a catch would appear frozen
        // because of a weather service the angler never asked about.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeout());
        factory.setReadTimeout(properties.readTimeout());

        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public Optional<WeatherConditions> getConditions(double latitude, double longitude, Instant caughtAt) {
        LocalDate date = caughtAt.atZone(ZoneOffset.UTC).toLocalDate();
        boolean useArchive = Duration.between(caughtAt, Instant.now()).compareTo(ARCHIVE_THRESHOLD) > 0;
        String baseUrl = useArchive ? properties.archiveUrl() : properties.forecastUrl();

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("hourly", HOURLY_FIELDS)
                .queryParam("wind_speed_unit", "ms")
                .queryParam("timezone", "UTC")
                .queryParam("start_date", date)
                .queryParam("end_date", date)
                .toUriString();

        try {
            OpenMeteoResponse response = restClient.get().uri(url).retrieve().body(OpenMeteoResponse.class);
            return toConditions(response, caughtAt);
        } catch (Exception e) {
            // Deliberately broad and deliberately swallowed: connection refused,
            // timeout, malformed JSON, rate limit - the caller's response to all
            // of them is identical, which is to carry on without weather.
            // Coordinates are kept out of the message, per the privacy rules.
            log.warn("Weather lookup failed ({}), continuing without conditions", e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    private Optional<WeatherConditions> toConditions(OpenMeteoResponse response, Instant caughtAt) {
        if (response == null || response.hourly() == null || response.hourly().time() == null) {
            return Optional.empty();
        }

        Hourly hourly = response.hourly();
        int index = nearestHourIndex(hourly.time(), caughtAt);
        if (index < 0) {
            return Optional.empty();
        }

        return Optional.of(new WeatherConditions(
                decimal(value(hourly.temperature(), index), 1),
                decimal(value(hourly.windSpeed(), index), 2),
                intValue(hourly.windDirection(), index),
                decimal(value(hourly.pressure(), index), 1),
                skyFrom(value(hourly.cloudCover(), index), value(hourly.precipitation(), index),
                        value(hourly.temperature(), index)),
                Instant.parse(hourly.time().get(index) + ":00Z")));
    }

    /**
     * Open-Meteo returns a whole day of hourly readings. The one that matters is
     * the hour closest to when the fish was actually caught.
     */
    private int nearestHourIndex(List<String> times, Instant caughtAt) {
        int best = -1;
        long bestDistance = Long.MAX_VALUE;
        for (int i = 0; i < times.size(); i++) {
            try {
                Instant hour = LocalDateTime.parse(times.get(i), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .toInstant(ZoneOffset.UTC);
                long distance = Math.abs(Duration.between(hour, caughtAt).toSeconds());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = i;
                }
            } catch (Exception ignored) {
                // A single unparseable timestamp should not lose the other 23.
            }
        }
        return best;
    }

    private SkyCondition skyFrom(Double cloudCoverPercent, Double precipitationMm, Double temperatureC) {
        if (precipitationMm != null && precipitationMm > 0) {
            return (temperatureC != null && temperatureC <= 0) ? SkyCondition.SNOW : SkyCondition.RAIN;
        }
        if (cloudCoverPercent == null) {
            return null;
        }
        if (cloudCoverPercent <= 10) return SkyCondition.CLEAR;
        if (cloudCoverPercent <= 40) return SkyCondition.PARTLY_CLOUDY;
        if (cloudCoverPercent <= 80) return SkyCondition.CLOUDY;
        return SkyCondition.OVERCAST;
    }

    private static Double value(List<Double> values, int index) {
        return (values == null || index >= values.size()) ? null : values.get(index);
    }

    private static Integer intValue(List<Integer> values, int index) {
        return (values == null || index >= values.size()) ? null : values.get(index);
    }

    /** Rounds to the scale the database column actually stores. */
    private static BigDecimal decimal(Double value, int scale) {
        return value == null ? null : BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OpenMeteoResponse(Hourly hourly) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Hourly(
            List<String> time,
            @JsonProperty("temperature_2m") List<Double> temperature,
            @JsonProperty("wind_speed_10m") List<Double> windSpeed,
            @JsonProperty("wind_direction_10m") List<Integer> windDirection,
            @JsonProperty("surface_pressure") List<Double> pressure,
            @JsonProperty("cloud_cover") List<Double> cloudCover,
            List<Double> precipitation) {
    }
}
