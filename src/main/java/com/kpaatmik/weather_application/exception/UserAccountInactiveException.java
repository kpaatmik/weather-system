package com.kpaatmik.weather_application.exception;

public class UserAccountInactiveException extends RuntimeException {

    public UserAccountInactiveException(String message) {
        super(message);
    }
}