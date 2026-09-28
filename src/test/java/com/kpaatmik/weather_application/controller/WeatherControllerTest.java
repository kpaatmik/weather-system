package com.kpaatmik.weather_application.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.kpaatmik.weather_application.dto.response.PublicCityResponse;
import com.kpaatmik.weather_application.dto.response.WeatherResponse;
import com.kpaatmik.weather_application.service.CityService;
import com.kpaatmik.weather_application.service.WeatherService;

class WeatherControllerTest {

	private CityService cityService;
	private WeatherService weatherService;

	private WeatherController weatherController;

	@BeforeEach
	void setUp() {

		cityService = mock(CityService.class);
		weatherService = mock(WeatherService.class);

		weatherController = new WeatherController(cityService, weatherService);
	}

	@Test
	void getWeather_shouldReturnWeather() {

		WeatherResponse weather = new WeatherResponse(1L, "KANNUR", 30.5, 31.0, 70, 4.5, "Clouds", "broken clouds");

		when(weatherService.getWeather(1L)).thenReturn(weather);

		ResponseEntity<WeatherResponse> response = weatherController.getWeather(1L);

		assertEquals(200, response.getStatusCode().value());
		assertEquals(weather, response.getBody());

		verify(weatherService).getWeather(1L);
	}

	@Test
	void getActiveCities_shouldReturnCities() {

		List<PublicCityResponse> cities = List.of(new PublicCityResponse(1L, "KANNUR", "KERALA", "IN"));

		when(cityService.getActiveCities()).thenReturn(cities);

		ResponseEntity<List<PublicCityResponse>> response = weatherController.getActiveCities();

		assertEquals(200, response.getStatusCode().value());
		assertEquals(cities, response.getBody());

		verify(cityService).getActiveCities();
	}
}