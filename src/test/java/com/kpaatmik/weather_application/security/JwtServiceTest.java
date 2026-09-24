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
        properties.setSecret("VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRGb3JUZXN0aW5n");
        properties.setAccessTokenExpiration(60_000);
        properties.setRefreshTokenExpiration(60_000);

        jwtService = new JwtService(properties);
    }

    @Test
    void generateAccessToken_shouldContainUsernameAndRole() {
        String token = jwtService.generateAccessToken("aatmik", "USER");

        assertEquals("aatmik", jwtService.extractUsername(token));
        assertEquals("USER", jwtService.extractRole(token));
        assertFalse(jwtService.isRefreshToken(token));
        assertTrue(jwtService.isTokenValid(token, "aatmik"));
    }

    @Test
    void generateRefreshToken_shouldContainRefreshType() {
        String token = jwtService.generateRefreshToken("aatmik");

        assertEquals("aatmik", jwtService.extractUsername(token));
        assertTrue(jwtService.isRefreshToken(token));
        assertTrue(jwtService.isTokenValid(token, "aatmik"));
    }

    @Test
    void isTokenValid_shouldRejectWrongUsername() {
        String token = jwtService.generateAccessToken("aatmik", "USER");

        assertFalse(jwtService.isTokenValid(token, "other"));
    }

    @Test
    void isTokenValid_shouldRejectMalformedToken() {
        assertFalse(jwtService.isTokenValid("not-a-jwt", "aatmik"));
    }

    @Test
    void isRefreshToken_shouldRejectAccessToken() {
        String token = jwtService.generateAccessToken("aatmik", "USER");

        assertFalse(jwtService.isRefreshToken(token));
    }

    @Test
    void isRefreshToken_shouldRejectMalformedToken() {
        assertFalse(jwtService.isRefreshToken("bad-token"));
    }

    @Test
    void extractUsername_shouldRejectMalformedToken() {
        assertThrows(Exception.class,
                () -> jwtService.extractUsername("bad-token"));
    }

    @Test
    void tokenShouldBeInvalidAfterExpiration() throws InterruptedException {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdTZWNyZXRGb3JUZXN0aW5n");
        properties.setAccessTokenExpiration(1);
        properties.setRefreshTokenExpiration(1);

        JwtService service = new JwtService(properties);
        String token = service.generateAccessToken("aatmik", "USER");

        Thread.sleep(20);

        assertFalse(service.isTokenValid(token, "aatmik"));
    }

    @Test
    void tokenSignedWithDifferentSecretShouldBeInvalid() {
        String token = jwtService.generateAccessToken("aatmik", "USER");

        JwtProperties otherProperties = new JwtProperties();
        otherProperties.setSecret("AnotherSecretThatIsLongEnoughForHS256SigningKey123456");
        otherProperties.setAccessTokenExpiration(60_000);
        otherProperties.setRefreshTokenExpiration(60_000);

        JwtService otherService = new JwtService(otherProperties);

        assertFalse(otherService.isTokenValid(token, "aatmik"));
    }
}
