package com.kpaatmik.weather_application.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void userAlreadyExists_shouldReturn409() {
        var response = handler.handleUserAlreadyExists(
                new UserAlreadyExistsException("Username already exists"));

        assertEquals(409, response.getStatusCode().value());
        assertEquals(409, response.getBody().status());
        assertEquals("Username already exists", response.getBody().message());
    }

    @Test
    void invalidCredentials_shouldReturn401() {
        var response = handler.handleInvalidCredentials(
                new InvalidCredentialsException("Invalid username or password"));

        assertEquals(401, response.getStatusCode().value());
        assertEquals(401, response.getBody().status());
    }

    @Test
    void invalidRefreshToken_shouldReturn401() {
        var response = handler.handleInvalidRefreshToken(
                new InvalidRefreshTokenException("Invalid refresh token"));

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void inactiveAccount_shouldReturn401() {
        var response = handler.handleInactiveAccount(
                new UserAccountInactiveException("User account is inactive"));

        assertEquals(401, response.getStatusCode().value());
    }

    @Test
    void cityNotFound_shouldReturn404() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/cities/weather/99");

        var response = handler.handleCityNotFound(
                new CityNotFoundException(99L), request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("CITY_NOT_FOUND", response.getBody().get("error"));
        assertEquals("/api/cities/weather/99", response.getBody().get("path"));
    }

    @Test
    void cityAlreadyExists_shouldReturn409() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/cities/save");

        var response = handler.handleCityAlreadyExists(
                new CityAlreadyExistsException("Kannur", "IN"), request);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("CITY_ALREADY_EXISTS", response.getBody().get("error"));
    }

    @Test
    void validation_shouldReturn400AndFieldErrors() {
        var target = new Object();
        var bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "username",
                "Username is required"));

        var ex = new MethodArgumentNotValidException(null, bindingResult);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/register");

        var response = handler.handleValidation(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDATION_FAILED", response.getBody().get("error"));
        assertEquals("Request validation failed", response.getBody().get("message"));

        @SuppressWarnings("unchecked")
        Map<String, String> errors =
                (Map<String, String>) response.getBody().get("errors");

        assertEquals("Username is required", errors.get("username"));
    }
}
