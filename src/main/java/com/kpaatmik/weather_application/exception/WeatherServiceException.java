package com.kpaatmik.weather_application.exception;

public class WeatherServiceException extends RuntimeException{
	public WeatherServiceException(String message,Throwable cause)
	{
		super(message,cause);
	}
}
