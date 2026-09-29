package com.kpaatmik.weatherapplication.dto.response;

public record WeatherResponse(

		Long cityId,

		String cityName,

		Double temperature,

		Double feelsLike,

		Integer humidity,

		Double windSpeed,

		String condition,

		String description

) {
}