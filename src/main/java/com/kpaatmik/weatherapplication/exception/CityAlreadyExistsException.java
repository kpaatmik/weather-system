package com.kpaatmik.weatherapplication.exception;

public class CityAlreadyExistsException extends RuntimeException {

	public CityAlreadyExistsException(String name, String country) {

		super("City '" + name + "' in country '" + country + "' is already configured");
	}
}