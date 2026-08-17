package com.example.catchcompass.conditions;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "catchcompass.weather")
public record WeatherProperties(
        String forecastUrl,
        String archiveUrl,
        Duration connectTimeout,
        Duration readTimeout) {
}
