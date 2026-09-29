package com.kpaatmik.weatherapplication.exception;

public class UserAccountInactiveException extends RuntimeException {

	public UserAccountInactiveException(String message) {
		super(message);
	}
}