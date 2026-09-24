package com.kpaatmik.weather_application.exception;

public class CityAlreadyExistsException extends RuntimeException {

	public CityAlreadyExistsException(String name, String country) {

		super("City '" + name + "' in country '" + country + "' is already configured");
	}
}