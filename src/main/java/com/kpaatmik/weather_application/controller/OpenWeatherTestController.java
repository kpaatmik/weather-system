package com.kpaatmik.weather_application.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class OpenWeatherTestController {

	private final RestClient restClient;

	// TEMPORARY: Replace with your actual API key
	private final String apiKey = "sdasda";

	public OpenWeatherTestController() {

		this.restClient = RestClient.builder().baseUrl("https://api.openweathermap.org").build();
	}

	@GetMapping("/test/weather")
	public String testWeather(@RequestParam double lat, @RequestParam double lon) {

		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/data/2.5/weather").queryParam("lat", lat).queryParam("lon", lon)
						.queryParam("appid", apiKey).queryParam("units", "metric").build())
				.retrieve().body(String.class);
	}

	@GetMapping("/test/geocoding")
	public String testGeocoding(@RequestParam String city) {

		return restClient.get().uri(uriBuilder -> uriBuilder.path("/geo/1.0/direct").queryParam("q", city)
				.queryParam("limit", 5).queryParam("appid", apiKey).build()).retrieve().body(String.class);
	}
}