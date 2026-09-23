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

import com.kpaatmik.weather_application.dto.CityResponse;
import com.kpaatmik.weather_application.dto.CitySuggestionResponse;
import com.kpaatmik.weather_application.dto.CreateCityRequest;
import com.kpaatmik.weather_application.service.CityService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/admin/cities")
@AllArgsConstructor
public class CityController {
	private final CityService cityService;
	
	  @GetMapping
	    public ResponseEntity<List<CityResponse>> getAllCities() {

	        return ResponseEntity.ok(
	                cityService.getAllCities()
	        );
	    }
	  @DeleteMapping("/{id}")
	    public ResponseEntity<Void> deleteCity(
	            @PathVariable Long id) {

	        cityService.deleteCity(id);

	        return ResponseEntity.noContent().build();
	    }
	  
	  @PatchMapping("/{cityId}/deactivate")
	  public ResponseEntity<CityResponse> deactivateCity(
	          @PathVariable Long cityId) {

	      return ResponseEntity.ok(
	              cityService.deactivateCity(cityId)
	      );
	  }
	
	
	@GetMapping("/sugesstion")
	public ResponseEntity<List<CitySuggestionResponse>> searchCities(@RequestParam String q) {
		
		return ResponseEntity.ok()
				.body(cityService.searchCities(q));
		
	}
	 @PostMapping("/save")
	    public ResponseEntity<CityResponse> createCity(
	            @Valid @RequestBody CreateCityRequest request) {
	
	        CityResponse response =
	                cityService.createCity(request);

	        return ResponseEntity
	                .status(HttpStatus.CREATED)
	                .body(response);
	    }	
	 
	
}
