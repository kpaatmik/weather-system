package com.kpaatmik.weatherapplication.exception;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String message, LocalDateTime timestamp, Object errors) {
}