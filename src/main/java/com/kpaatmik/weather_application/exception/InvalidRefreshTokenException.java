package com.kpaatmik.weather_application.exception;

public class InvalidRefreshTokenException extends RuntimeException {

	public InvalidRefreshTokenException(String message) {
		super(message);
	}
}