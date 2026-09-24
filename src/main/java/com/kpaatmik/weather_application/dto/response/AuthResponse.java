package com.kpaatmik.weather_application.dto.response;

public record AuthResponse(

		String accessToken,

		String tokenType,

		String username,

		String role

) {
}