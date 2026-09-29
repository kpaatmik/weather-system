package com.kpaatmik.weatherapplication.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.kpaatmik.weatherapplication.controller.CityController;
import com.kpaatmik.weatherapplication.dto.request.CreateCityRequest;
import com.kpaatmik.weatherapplication.dto.response.CityResponse;
import com.kpaatmik.weatherapplication.dto.response.CitySuggestionResponse;
import com.kpaatmik.weatherapplication.service.CityService;

class CityControllerTest {

	private CityService cityService;

	private CityController cityController;

	@BeforeEach
	void setUp() {

		cityService = mock(CityService.class);

		cityController = new CityController(cityService);
	}

	@Test
	void getAllCities_shouldReturnCities() {

		List<CityResponse> cities = List
				.of(new CityResponse(1L, "KANNUR", "KERALA", "IN", 11.87, 75.37, true, null, null));

		when(cityService.getAllCities()).thenReturn(cities);

		ResponseEntity<List<CityResponse>> response = cityController.getAllCities();

		assertEquals(200, response.getStatusCode().value());
		assertEquals(cities, response.getBody());

		verify(cityService).getAllCities();
	}

	@Test
	void deleteCity_shouldReturnNoContent() {

		ResponseEntity<Void> response = cityController.deleteCity(1L);

		assertEquals(204, response.getStatusCode().value());

		assertNull(response.getBody());

		verify(cityService).deleteCity(1L);
	}

	@Test
	void deactivateCity_shouldReturnUpdatedCity() {

		CityResponse city = new CityResponse(1L, "KANNUR", "KERALA", "IN", 11.87, 75.37, false, null, null);

		when(cityService.deactivateCity(1L)).thenReturn(city);

		ResponseEntity<CityResponse> response = cityController.deactivateCity(1L);

		assertEquals(200, response.getStatusCode().value());
		assertEquals(city, response.getBody());

		verify(cityService).deactivateCity(1L);
	}

	@Test
	void searchCities_shouldReturnSuggestions() {

		List<CitySuggestionResponse> suggestions = List
				.of(new CitySuggestionResponse("Kannur", "Kerala", "IN", 11.8745, 75.3704));

		when(cityService.searchCities("Kannur")).thenReturn(suggestions);

		ResponseEntity<List<CitySuggestionResponse>> response = cityController.searchCities("Kannur");

		assertEquals(200, response.getStatusCode().value());
		assertEquals(suggestions, response.getBody());

		verify(cityService).searchCities("Kannur");
	}

	@Test
	void createCity_shouldReturnCreated() {

		CreateCityRequest request = new CreateCityRequest("Kannur", "Kerala", "IN", 11.8745, 75.3704);

		CityResponse city = new CityResponse(1L, "KANNUR", "KERALA", "IN", 11.8745, 75.3704, true, null, null);

		when(cityService.createCity(request)).thenReturn(city);

		ResponseEntity<CityResponse> response = cityController.createCity(request);

		assertEquals(201, response.getStatusCode().value());
		assertEquals(city, response.getBody());

		verify(cityService).createCity(request);
	}
}