package com.kpaatmik.weatherapplication.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class OpenWeatherPropertiesTest {

    @Test
    void shouldSetAndGetAllProperties() {

        OpenWeatherProperties properties =
                new OpenWeatherProperties();

        properties.setApiKey("test-api-key");
        properties.setWeatherBaseUrl(
                "https://api.openweathermap.org/data/2.5"
        );
        properties.setGeocodingBaseUrl(
                "https://api.openweathermap.org/geo/1.0"
        );
        properties.setConnectTimeout(
                Duration.ofSeconds(5)
        );
        properties.setReadTimeout(
                Duration.ofSeconds(10)
        );

        assertEquals(
                "test-api-key",
                properties.getApiKey()
        );

        assertEquals(
                "https://api.openweathermap.org/data/2.5",
                properties.getWeatherBaseUrl()
        );

        assertEquals(
                "https://api.openweathermap.org/geo/1.0",
                properties.getGeocodingBaseUrl()
        );

        assertEquals(
                Duration.ofSeconds(5),
                properties.getConnectTimeout()
        );

        assertEquals(
                Duration.ofSeconds(10),
                properties.getReadTimeout()
        );
    }
}