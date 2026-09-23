package com.kpaatmik.weather_application.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kpaatmik.weather_application.dto.request.LoginRequest;
import com.kpaatmik.weather_application.dto.request.RegisterRequest;
import com.kpaatmik.weather_application.dto.response.AuthResponse;
import com.kpaatmik.weather_application.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.ok(
                "User registered successfully"
        );
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response =
                authService.login(request);

        String refreshToken =
                authService.generateRefreshToken(
                        response.username()
                );

        ResponseCookie refreshCookie =
                createRefreshCookie(refreshToken);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie.toString()
                )
                .body(response);
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request) {

        String refreshToken =
                extractRefreshToken(request);

        AuthResponse response =
                authService.refresh(refreshToken);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        ResponseCookie cookie =
                clearRefreshCookie();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body("Logged out successfully");
    }


    private String extractRefreshToken(
            HttpServletRequest request) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {

            if ("refresh_token".equals(
                    cookie.getName())) {

                return cookie.getValue();
            }
        }

        return null;
    }


    private ResponseCookie createRefreshCookie(
            String refreshToken) {

        return ResponseCookie
                .from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false) // true in production HTTPS
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60)
                .build();
    }


    private ResponseCookie clearRefreshCookie() {

        return ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(false) // true in production HTTPS
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}