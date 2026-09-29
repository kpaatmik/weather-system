package com.kpaatmik.weatherapplication.dto.response;

public record AuthResponse(

		String accessToken,

		String tokenType,

		String username,

		String role

) {
}