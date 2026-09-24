package com.kpaatmik.weather_application.dto.response;

import java.util.Map;

public record OpenWeatherGeocodingResponse(String name,

		Map<String, String> localNames,

		Double lat,

		Double lon,

		String country,

		String state) {

}
