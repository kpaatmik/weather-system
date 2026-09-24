package com.kpaatmik.weather_application.dto.response;

import java.time.LocalDateTime;

public record CityResponse(Long id, String name, String state, String country, Double latitude, Double longitude,
		Boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {

}
