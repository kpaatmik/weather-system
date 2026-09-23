package com.kpaatmik.weather_application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kpaatmik.weather_application.dto.PublicCityResponse;
import com.kpaatmik.weather_application.dto.WeatherResponse;
import com.kpaatmik.weather_application.service.CityService;
import com.kpaatmik.weather_application.service.WeatherService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("api/cities")
@AllArgsConstructor
public class WeatherController {
	private final CityService cityService;
	private final WeatherService weatherService;
	
	@GetMapping("/weather/{cityId}")
	 public ResponseEntity<WeatherResponse> getWeather(
	            @PathVariable Long cityId) {

	        return ResponseEntity.ok(
	                weatherService.getWeather(cityId)
	        );
	    }
	@GetMapping("/active")
	public ResponseEntity<List<PublicCityResponse>> getActiveCities() {

	    return ResponseEntity.ok(
	            cityService.getActiveCities()
	    );
	}
	
	

}
