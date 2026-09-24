package com.kpaatmik.weather_application.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;
import jakarta.validation.*;
import com.kpaatmik.weather_application.dto.request.*;

class RequestValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void registerRequest_validRequest_shouldHaveNoViolations() {
        var request = new RegisterRequest("aatmik", "aatmik@example.com", "password123");
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void registerRequest_shouldRejectShortUsername() {
        var request = new RegisterRequest("ab", "aatmik@example.com", "password123");
        assertHasProperty(request, "username");
    }

    @Test
    void registerRequest_shouldRejectInvalidEmail() {
        var request = new RegisterRequest("aatmik", "invalid-email", "password123");
        assertHasProperty(request, "email");
    }

    @Test
    void registerRequest_shouldRejectShortPassword() {
        var request = new RegisterRequest("aatmik", "aatmik@example.com", "1234567");
        assertHasProperty(request, "password");
    }

    @Test
    void loginRequest_shouldRejectBlankFields() {
        var request = new LoginRequest("", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertEquals(2, violations.size());
    }

    @Test
    void createCityRequest_shouldAcceptValidCoordinates() {
        var request = new CreateCityRequest("Kannur", "Kerala", "IN", 11.8745, 75.3704);
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void createCityRequest_shouldRejectMissingRequiredFields() {
        var request = new CreateCityRequest("", null, "", null, null);
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void createCityRequest_shouldRejectLatitudeBelowMinus90() {
        var request = new CreateCityRequest("Kannur", "Kerala", "IN", -90.01, 75.0);
        assertHasProperty(request, "latitude");
    }

    @Test
    void createCityRequest_currentImplementationDoesNotEnforceLatitudeUpperBound() {
        var request = new CreateCityRequest("Kannur", "Kerala", "IN", 90.01, 75.0);
        // This documents a real gap in the current DTO:
        // @DecimalMax(90.0) is missing from latitude.
        assertFalse(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude")));
    }

    @Test
    void createCityRequest_currentImplementationDoesNotEnforceLongitudeLowerBound() {
        var request = new CreateCityRequest("Kannur", "Kerala", "IN", 10.0, -180.01);
        // This documents a real gap in the current DTO:
        // @DecimalMin(-180.0) is missing from longitude.
        assertFalse(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude")));
    }

    @Test
    void createCityRequest_shouldRejectLongitudeAbove180() {
        var request = new CreateCityRequest("Kannur", "Kerala", "IN", 10.0, 180.01);
        assertHasProperty(request, "longitude");
    }

    private <T> void assertHasProperty(T request, String property) {
        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(property)));
    }
}
