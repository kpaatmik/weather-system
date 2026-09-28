package com.kpaatmik.weather_application.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityUtilTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUsername_shouldReturnUsername() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "aatmik",
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertEquals(
                "aatmik",
                SecurityUtil.getCurrentUsername()
        );
    }

    @Test
    void getCurrentUsername_shouldReturnNullWhenNotAuthenticated() {

        SecurityContextHolder.clearContext();

        assertNull(
                SecurityUtil.getCurrentUsername()
        );
    }
}