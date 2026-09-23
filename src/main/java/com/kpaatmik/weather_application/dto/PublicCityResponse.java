package com.kpaatmik.weather_application.dto;

public record PublicCityResponse(
        Long id,
        String name,
        String state,
        String country
) {
}