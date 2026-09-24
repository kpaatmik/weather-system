package com.kpaatmik.weather_application.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kpaatmik.weather_application.audit.AuditAction;
import com.kpaatmik.weather_application.audit.AuditEntityType;
import com.kpaatmik.weather_application.dto.request.LoginRequest;
import com.kpaatmik.weather_application.dto.request.RegisterRequest;
import com.kpaatmik.weather_application.dto.response.AuthResponse;
import com.kpaatmik.weather_application.entity.Role;
import com.kpaatmik.weather_application.entity.User;
import com.kpaatmik.weather_application.exception.InvalidCredentialsException;
import com.kpaatmik.weather_application.exception.InvalidRefreshTokenException;
import com.kpaatmik.weather_application.exception.UserAccountInactiveException;
import com.kpaatmik.weather_application.exception.UserAlreadyExistsException;
import com.kpaatmik.weather_application.repository.UserRepository;
import com.kpaatmik.weather_application.security.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final AuditService auditService;

	@Transactional
	public void register(RegisterRequest request) {

		log.info("Registration attempt for username={}, email={}", request.username(), request.email());

		if (userRepository.existsByUsername(request.username())) {

			log.warn("Registration failed: username already exists, username={}", request.username());

			throw new UserAlreadyExistsException("Username already exists");
		}

		if (userRepository.existsByEmail(request.email())) {

			log.warn("Registration failed: email already exists, email={}", request.email());

			throw new UserAlreadyExistsException("Email already exists");
		}

		User user = User.builder().username(request.username()).email(request.email())
				.password(passwordEncoder.encode(request.password())).role(Role.USER).active(true).build();

		User savedUser = userRepository.save(user);

		auditService.log(savedUser.getId(), AuditAction.USER_REGISTERED, AuditEntityType.USER, savedUser.getId(),
				"User registered successfully");

		log.info("User registered successfully: userId={}, username={}", savedUser.getId(), savedUser.getUsername());
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {

		log.info("Login attempt for username={}", request.username());

		Authentication authentication;

		try {

			authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

			log.debug("Authentication successful for username={}", request.username());

		} catch (AuthenticationException ex) {

			log.warn("Authentication failed for username={}", request.username());

			throw new InvalidCredentialsException("Invalid username or password");
		}

		User user = userRepository.findByUsername(request.username()).orElseThrow(() -> {
			log.warn("Authenticated user not found in database: username={}", request.username());

			return new InvalidCredentialsException("Invalid username or password");
		});

		if (!Boolean.TRUE.equals(user.getActive())) {

			log.warn("Login rejected: inactive user, username={}, userId={}", user.getUsername(), user.getId());

			throw new UserAccountInactiveException("User account is inactive");
		}

		String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole().name());

		auditService.log(user.getId(), AuditAction.USER_LOGIN, AuditEntityType.USER, user.getId(),
				"User logged in successfully");

		log.info("User logged in successfully: userId={}, username={}, role={}", user.getId(), user.getUsername(),
				user.getRole());

		return new AuthResponse(accessToken, "Bearer", user.getUsername(), user.getRole().name());
	}

	public String generateRefreshToken(String username) {

		log.debug("Generating refresh token for username={}", username);

		return jwtService.generateRefreshToken(username);
	}

	public AuthResponse refresh(String refreshToken) {

		log.debug("Refresh token request received");

		if (refreshToken == null || refreshToken.isBlank()) {

			log.warn("Refresh token request rejected: token is missing");

			throw new InvalidRefreshTokenException("Refresh token is missing");
		}

		if (!jwtService.isRefreshToken(refreshToken)) {

			log.warn("Refresh token request rejected: invalid token type");

			throw new InvalidRefreshTokenException("Invalid refresh token");
		}

		String username;

		try {

			username = jwtService.extractUsername(refreshToken);

			log.debug("Username extracted from refresh token: username={}", username);

		} catch (Exception ex) {

			log.warn("Refresh token rejected: unable to extract username");

			throw new InvalidRefreshTokenException("Invalid or expired refresh token");
		}

		User user = userRepository.findByUsername(username).orElseThrow(() -> {

			log.warn("Refresh token rejected: user not found, username={}", username);

			return new InvalidRefreshTokenException("Invalid refresh token");
		});

		if (!Boolean.TRUE.equals(user.getActive())) {

			log.warn("Refresh token rejected: inactive user, username={}, userId={}", user.getUsername(), user.getId());

			throw new UserAccountInactiveException("User account is inactive");
		}

		if (!jwtService.isTokenValid(refreshToken, username)) {

			log.warn("Refresh token rejected: token validation failed, username={}", username);

			throw new InvalidRefreshTokenException("Invalid or expired refresh token");
		}

		String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole().name());

		log.info("Access token refreshed successfully: userId={}, username={}", user.getId(), user.getUsername());

		return new AuthResponse(accessToken, "Bearer", user.getUsername(), user.getRole().name());
	}
}