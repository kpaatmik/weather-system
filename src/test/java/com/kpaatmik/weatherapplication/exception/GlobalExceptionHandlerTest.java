package com.kpaatmik.weatherapplication.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    // =========================================================
    // USER ALREADY EXISTS
    // =========================================================

    @Test
    void handleUserAlreadyExists_shouldReturn409() {

        UserAlreadyExistsException exception =
                mock(UserAlreadyExistsException.class);

        when(exception.getMessage())
                .thenReturn("Username already exists");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleUserAlreadyExists(exception);

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals(409, body.status());

        assertEquals(
                "Username already exists",
                body.message()
        );

        assertNotNull(body.timestamp());

        assertNull(body.errors());
    }


    // =========================================================
    // INVALID CREDENTIALS
    // =========================================================

    @Test
    void handleInvalidCredentials_shouldReturn401() {

        InvalidCredentialsException exception =
                mock(InvalidCredentialsException.class);

        when(exception.getMessage())
                .thenReturn("Invalid username or password");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleInvalidCredentials(exception);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals(401, body.status());

        assertEquals(
                "Invalid username or password",
                body.message()
        );

        assertNotNull(body.timestamp());

        assertNull(body.errors());
    }


    // =========================================================
    // INVALID REFRESH TOKEN
    // =========================================================

    @Test
    void handleInvalidRefreshToken_shouldReturn401() {

        InvalidRefreshTokenException exception =
                mock(InvalidRefreshTokenException.class);

        when(exception.getMessage())
                .thenReturn("Invalid refresh token");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleInvalidRefreshToken(exception);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals(401, body.status());

        assertEquals(
                "Invalid refresh token",
                body.message()
        );

        assertNotNull(body.timestamp());

        assertNull(body.errors());
    }


    // =========================================================
    // INACTIVE USER ACCOUNT
    // =========================================================

    @Test
    void handleInactiveAccount_shouldReturn401() {

        UserAccountInactiveException exception =
                mock(UserAccountInactiveException.class);

        when(exception.getMessage())
                .thenReturn("User account is inactive");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleInactiveAccount(exception);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals(401, body.status());

        assertEquals(
                "User account is inactive",
                body.message()
        );

        assertNotNull(body.timestamp());

        assertNull(body.errors());
    }


    // =========================================================
    // CITY NOT FOUND
    // =========================================================

    @Test
    void handleCityNotFound_shouldReturn404() {

        CityNotFoundException exception =
                mock(CityNotFoundException.class);

        when(exception.getMessage())
                .thenReturn("City not found");

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/cities/weather/10");

        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleCityNotFound(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();

        assertEquals(
                404,
                body.get("status")
        );

        assertEquals(
                "CITY_NOT_FOUND",
                body.get("error")
        );

        assertEquals(
                "City not found",
                body.get("message")
        );

        assertEquals(
                "/api/cities/weather/10",
                body.get("path")
        );

        assertNotNull(body.get("timestamp"));
    }


    // =========================================================
    // CITY ALREADY EXISTS
    // =========================================================

    @Test
    void handleCityAlreadyExists_shouldReturn409() {

        CityAlreadyExistsException exception =
                mock(CityAlreadyExistsException.class);

        when(exception.getMessage())
                .thenReturn("City already exists");

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/admin/cities/save");

        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleCityAlreadyExists(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();

        assertEquals(
                409,
                body.get("status")
        );

        assertEquals(
                "CITY_ALREADY_EXISTS",
                body.get("error")
        );

        assertEquals(
                "City already exists",
                body.get("message")
        );

        assertEquals(
                "/api/admin/cities/save",
                body.get("path")
        );

        assertNotNull(body.get("timestamp"));
    }


    // =========================================================
    // VALIDATION ERROR
    // =========================================================

    @Test
    void handleValidation_shouldReturn400WithFieldErrors() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError usernameError =
                new FieldError(
                        "registerRequest",
                        "username",
                        "Username is required"
                );

        FieldError passwordError =
                new FieldError(
                        "registerRequest",
                        "password",
                        "Password must contain at least 8 characters"
                );

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(
                        List.of(
                                usernameError,
                                passwordError
                        )
                );

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/auth/register");

        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleValidation(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();

        assertEquals(
                400,
                body.get("status")
        );

        assertEquals(
                "VALIDATION_FAILED",
                body.get("error")
        );

        assertEquals(
                "Request validation failed",
                body.get("message")
        );

        assertEquals(
                "/api/auth/register",
                body.get("path")
        );

        assertNotNull(body.get("timestamp"));

        @SuppressWarnings("unchecked")
        Map<String, String> errors =
                (Map<String, String>) body.get("errors");

        assertNotNull(errors);

        assertEquals(2, errors.size());

        assertEquals(
                "Username is required",
                errors.get("username")
        );

        assertEquals(
                "Password must contain at least 8 characters",
                errors.get("password")
        );
    }


    // =========================================================
    // WEATHER SERVICE EXCEPTION
    // =========================================================

    @Test
    void handleWeatherServiceException_shouldReturn503() {

        WeatherServiceException exception =
                mock(WeatherServiceException.class);

        when(exception.getMessage())
                .thenReturn(
                        "Unable to connect to OpenWeather API"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleWeatherServiceException(
                        exception
                );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals(503, body.status());

        assertEquals(
                "Unable to connect to OpenWeather API",
                body.message()
        );

        assertNotNull(body.timestamp());

        assertNull(body.errors());
    }


    // =========================================================
    // DATABASE EXCEPTION
    // =========================================================

    @Test
    void handleDatabaseException_shouldReturn500() {

        DataAccessException exception =
                new DataAccessException(
                        "Database connection failed"
                ) {
                };

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleDatabaseException(
                        exception
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals(500, body.status());

        assertEquals(
                "A database error occurred",
                body.message()
        );

        assertNotNull(body.timestamp());

        assertNull(body.errors());
    }
}