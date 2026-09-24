package com.kpaatmik.weather_application.exception;

public class CityNotFoundException extends RuntimeException {

	public CityNotFoundException(Long id) {
		super("City with id " + id + " was not found");
	}
}