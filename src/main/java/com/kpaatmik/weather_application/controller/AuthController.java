package com.kpaatmik.weather_application.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kpaatmik.weather_application.audit.AuditAction;
import com.kpaatmik.weather_application.audit.AuditEntityType;
import com.kpaatmik.weather_application.dto.request.LoginRequest;
import com.kpaatmik.weather_application.dto.request.RegisterRequest;
import com.kpaatmik.weather_application.dto.response.AuthResponse;
import com.kpaatmik.weather_application.security.SecurityUtil;
import com.kpaatmik.weather_application.service.AuditService;
import com.kpaatmik.weather_application.service.AuthService;
import com.kpaatmik.weather_application.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user registration, login, token refresh and logout")
public class AuthController {

	private final AuthService authService;
	private final AuditService auditService;
	private final UserService userService;

	@Operation(summary = "Register a new user", description = "Creates a new user account with the USER role.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "User registered successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid registration data"),
			@ApiResponse(responseCode = "409", description = "Username or email already exists") })
	@PostMapping("/register")
	public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {

		authService.register(request);

		return ResponseEntity.ok("User registered successfully");
	}

	@Operation(summary = "Login user", description = "Authenticates a user and returns an access token. A refresh token is stored in an HttpOnly cookie.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Login successful"),
			@ApiResponse(responseCode = "400", description = "Invalid login request"),
			@ApiResponse(responseCode = "401", description = "Invalid username or password") })
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

		AuthResponse response = authService.login(request);

		String refreshToken = authService.generateRefreshToken(response.username());

		ResponseCookie refreshCookie = createRefreshCookie(refreshToken);

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).body(response);
	}

	@Operation(summary = "Refresh access token", description = "Generates a new access token using the refresh token stored in the HttpOnly cookie.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Access token refreshed successfully"),
			@ApiResponse(responseCode = "401", description = "Missing, invalid or expired refresh token") })
	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {

		String refreshToken = extractRefreshToken(request);

		AuthResponse response = authService.refresh(refreshToken);

		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Logout user", description = "Clears the refresh token cookie from the client.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Logged out successfully") })
	@PostMapping("/logout")
	public ResponseEntity<String> logout() {

		ResponseCookie cookie = clearRefreshCookie();

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body("Logged out successfully");
	}

	private String extractRefreshToken(HttpServletRequest request) {

		if (request.getCookies() == null) {
			return null;
		}

		for (Cookie cookie : request.getCookies()) {

			if ("refresh_token".equals(cookie.getName())) {

				return cookie.getValue();
			}
		}

		return null;
	}

	private ResponseCookie createRefreshCookie(String refreshToken) {

		return ResponseCookie.from("refresh_token", refreshToken).httpOnly(true).secure(false) // true in production
																								// HTTPS
				.sameSite("Strict").path("/api/auth").maxAge(7 * 24 * 60 * 60).build();
	}

	private ResponseCookie clearRefreshCookie() {
		Long userId = userService.getUserId(SecurityUtil.getCurrentUsername());
		auditService.log(userId, AuditAction.USER_LOGOUT, AuditEntityType.USER, userId, "User loged out: " + userId

		);

		return ResponseCookie.from("refresh_token", "").httpOnly(true).secure(false) // true in production HTTPS
				.sameSite("Strict").path("/api/auth").maxAge(0).build();
	}
}