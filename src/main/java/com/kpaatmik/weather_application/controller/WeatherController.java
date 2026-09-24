package com.kpaatmik.weather_application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kpaatmik.weather_application.dto.response.PublicCityResponse;
import com.kpaatmik.weather_application.dto.response.WeatherResponse;
import com.kpaatmik.weather_application.service.CityService;
import com.kpaatmik.weather_application.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("api/cities")
@AllArgsConstructor
@Tag(name = "Weather", description = "APIs for viewing configured cities and retrieving weather information")
@SecurityRequirement(name = "bearerAuth")
public class WeatherController {

	private final CityService cityService;
	private final WeatherService weatherService;

	@Operation(summary = "Get weather for a city", description = "Retrieves the current weather information for a configured city. Weather data may be served from the application cache.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Weather information retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Access denied"),
			@ApiResponse(responseCode = "404", description = "City not found"),
			@ApiResponse(responseCode = "502", description = "External weather service failure") })
	@GetMapping("/weather/{cityId}")
	public ResponseEntity<WeatherResponse> getWeather(@PathVariable Long cityId) {

		return ResponseEntity.ok(weatherService.getWeather(cityId));
	}

	@Operation(summary = "Get active cities", description = "Returns the list of cities currently active and available for weather searches.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Active cities retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@GetMapping("/active")
	public ResponseEntity<List<PublicCityResponse>> getActiveCities() {

		return ResponseEntity.ok(cityService.getActiveCities());
	}
}