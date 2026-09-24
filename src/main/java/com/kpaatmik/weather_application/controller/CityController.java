package com.kpaatmik.weather_application.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kpaatmik.weather_application.dto.request.CreateCityRequest;
import com.kpaatmik.weather_application.dto.response.CityResponse;
import com.kpaatmik.weather_application.dto.response.CitySuggestionResponse;
import com.kpaatmik.weather_application.service.CityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/admin/cities")
@AllArgsConstructor
@Tag(name = "Admin - City Management", description = "APIs for administrators to search, add, view, deactivate and delete configured cities")
@SecurityRequirement(name = "bearerAuth")
public class CityController {

	private final CityService cityService;

	@Operation(summary = "Get all configured cities", description = "Returns all cities configured in the system, including their active status.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Cities retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@GetMapping
	public ResponseEntity<List<CityResponse>> getAllCities() {

		return ResponseEntity.ok(cityService.getAllCities());
	}

	@Operation(summary = "Delete a city", description = "Permanently deletes a configured city from the system.")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "City deleted successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Admin access required"),
			@ApiResponse(responseCode = "404", description = "City not found") })
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCity(@PathVariable Long id) {

		cityService.deleteCity(id);

		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Deactivate a city", description = "Marks a configured city as inactive. Inactive cities are not available to normal users for weather searches.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "City deactivated successfully"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Admin access required"),
			@ApiResponse(responseCode = "404", description = "City not found") })
	@PatchMapping("/{cityId}/deactivate")
	public ResponseEntity<CityResponse> deactivateCity(@PathVariable Long cityId) {

		return ResponseEntity.ok(cityService.deactivateCity(cityId));
	}

	@Operation(summary = "Search cities", description = "Searches for cities using the external OpenWeather geocoding service. The returned suggestions can be used when configuring a city.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "City suggestions retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid or insufficient search query"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@GetMapping("/sugesstion")
	public ResponseEntity<List<CitySuggestionResponse>> searchCities(@RequestParam String q) {

		return ResponseEntity.ok().body(cityService.searchCities(q));
	}

	@Operation(summary = "Add a configured city", description = "Adds a city to the system using the selected city information.")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "City created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid city data"),
			@ApiResponse(responseCode = "401", description = "Authentication required"),
			@ApiResponse(responseCode = "403", description = "Admin access required"),
			@ApiResponse(responseCode = "409", description = "City already exists") })
	@PostMapping("/save")
	public ResponseEntity<CityResponse> createCity(@Valid @RequestBody CreateCityRequest request) {

		CityResponse response = cityService.createCity(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}