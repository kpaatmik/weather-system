package com.kpaatmik.weather_application.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kpaatmik.weather_application.config.JwtProperties;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        JwtProperties properties = new JwtProperties();

        properties.setSecret(
                "this-is-a-very-long-secret-key-for-testing-123456"
        );

        properties.setAccessTokenExpiration(15 * 60 * 1000);
        properties.setRefreshTokenExpiration(7 * 24 * 60 * 60 * 1000);

        jwtService = new JwtService(properties);
    }

    @Test
    void generateAccessToken_shouldCreateValidToken() {

        String token =
                jwtService.generateAccessToken("aatmik", "USER");

        assertNotNull(token);
        assertEquals("aatmik", jwtService.extractUsername(token));
        assertEquals("USER", jwtService.extractRole(token));
    }

    @Test
    void generateRefreshToken_shouldCreateRefreshToken() {

        String token =
                jwtService.generateRefreshToken("aatmik");

        assertNotNull(token);
        assertEquals("aatmik", jwtService.extractUsername(token));
        assertTrue(jwtService.isRefreshToken(token));
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {

        String token =
                jwtService.generateAccessToken("aatmik", "USER");

        assertTrue(
                jwtService.isTokenValid(token, "aatmik")
        );
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUsername() {

        String token =
                jwtService.generateAccessToken("aatmik", "USER");

        assertFalse(
                jwtService.isTokenValid(token, "admin")
        );
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {

        assertFalse(
                jwtService.isTokenValid("invalid-token", "aatmik")
        );
    }

    @Test
    void isRefreshToken_shouldReturnFalseForAccessToken() {

        String token =
                jwtService.generateAccessToken("aatmik", "USER");

        assertFalse(
                jwtService.isRefreshToken(token)
        );
    }

    @Test
    void isRefreshToken_shouldReturnFalseForInvalidToken() {

        assertFalse(
                jwtService.isRefreshToken("invalid-token")
        );
    }
}