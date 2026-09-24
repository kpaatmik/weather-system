package com.kpaatmik.weather_application.exception;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String message, LocalDateTime timestamp, Object errors) {
}