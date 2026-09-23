package com.kpaatmik.weather_application.service;

import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;


    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(
                request.username())) {

            throw new UserAlreadyExistsException(
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(
                request.email())) {

            throw new UserAlreadyExistsException(
                    "Email already exists"
            );
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(
                        passwordEncoder.encode(
                                request.password()
                        )
                )
                .role(Role.USER)
                .active(true)
                .build();

        userRepository.save(user);
    }


    public AuthResponse login(LoginRequest request) {

        Authentication authentication;

        try {

            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.username(),
                                    request.password()
                            )
                    );

        } catch (AuthenticationException ex) {

            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }

        User user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid username or password"
                        )
                );

        if (!Boolean.TRUE.equals(user.getActive())) {

            throw new UserAccountInactiveException(
                    "User account is inactive"
            );
        }

        String accessToken =
                jwtService.generateAccessToken(
                        user.getUsername(),
                        user.getRole().name()
                );

        return new AuthResponse(
                accessToken,
                "Bearer",
                user.getUsername(),
                user.getRole().name()
        );
    }


    public String generateRefreshToken(String username) {

        return jwtService.generateRefreshToken(username);
    }


    public AuthResponse refresh(String refreshToken) {

        if (refreshToken == null ||
                refreshToken.isBlank()) {

            throw new InvalidRefreshTokenException(
                    "Refresh token is missing"
            );
        }

        if (!jwtService.isRefreshToken(refreshToken)) {

            throw new InvalidRefreshTokenException(
                    "Invalid refresh token"
            );
        }

        String username;

        try {

            username =
                    jwtService.extractUsername(
                            refreshToken
                    );

        } catch (Exception ex) {

            throw new InvalidRefreshTokenException(
                    "Invalid or expired refresh token"
            );
        }

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(
                                "Invalid refresh token"
                        )
                );

        if (!Boolean.TRUE.equals(user.getActive())) {

            throw new UserAccountInactiveException(
                    "User account is inactive"
            );
        }

        if (!jwtService.isTokenValid(
                refreshToken,
                username
        )) {

            throw new InvalidRefreshTokenException(
                    "Invalid or expired refresh token"
            );
        }

        String accessToken =
                jwtService.generateAccessToken(
                        user.getUsername(),
                        user.getRole().name()
                );

        return new AuthResponse(
                accessToken,
                "Bearer",
                user.getUsername(),
                user.getRole().name()
        );
    }
}