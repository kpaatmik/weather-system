package com.kpaatmik.weather_application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCityRequest(
		@NotBlank(message = "City name is required") @Size(max = 100, message = "City name must not exceed 100 characters") String name,
		@Size(max = 100, message = "State must not exceed 100 characters") String state,
		@NotBlank(message = "Country is required") @Size(max = 10, message = "Country must not exceed 10 characters") String country,
		@NotNull(message = "Latitude is required") @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90") Double latitude,
		@DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180") Double longitude) {

}
