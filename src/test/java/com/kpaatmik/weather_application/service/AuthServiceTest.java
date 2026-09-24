package com.kpaatmik.weather_application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

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

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private AuditService auditService;

    private AuthService authService;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        auditService = mock(AuditService.class);

        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                auditService
        );
    }

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------

    @Test
    void register_shouldCreateUserSuccessfully() {

        RegisterRequest request =
                new RegisterRequest(
                        "john",
                        "john@gmail.com",
                        "password123"
                );

        User savedUser = User.builder()
                .id(1L)
                .username("john")
                .email("john@gmail.com")
                .password("encoded-password")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        authService.register(request);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("john")
                        && user.getEmail().equals("john@gmail.com")
                        && user.getPassword().equals("encoded-password")
                        && user.getRole() == Role.USER
                        && Boolean.TRUE.equals(user.getActive())
        ));

        verify(auditService).log(
                eq(1L),
                eq(AuditAction.USER_REGISTERED),
                eq(AuditEntityType.USER),
                eq(1L),
                eq("User registered successfully")
        );
    }

    @Test
    void register_shouldRejectDuplicateUsername() {

        RegisterRequest request =
                new RegisterRequest(
                        "john",
                        "john@gmail.com",
                        "password123"
                );

        when(userRepository.existsByUsername("john"))
                .thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any());
        verify(auditService, never()).log(
                anyLong(),
                any(),
                any(),
                anyLong(),
                anyString()
        );
    }

    @Test
    void register_shouldRejectDuplicateEmail() {

        RegisterRequest request =
                new RegisterRequest(
                        "john",
                        "john@gmail.com",
                        "password123"
                );

        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------

    @Test
    void login_shouldReturnAccessToken() {

        LoginRequest request =
                new LoginRequest(
                        "john",
                        "password123"
                );

        User user = User.builder()
                .id(1L)
                .username("john")
                .email("john@gmail.com")
                .password("encoded-password")
                .role(Role.USER)
                .active(true)
                .build();

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(mock(org.springframework.security.core.Authentication.class));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken("john", "USER"))
                .thenReturn("access-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("john", response.username());
        assertEquals("USER", response.role());

        verify(auditService).log(
                eq(1L),
                eq(AuditAction.USER_LOGIN),
                eq(AuditEntityType.USER),
                eq(1L),
                eq("User logged in successfully")
        );
    }

    @Test
    void login_shouldRejectInvalidCredentials() {

        LoginRequest request =
                new LoginRequest(
                        "john",
                        "wrong-password"
                );

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenThrow(
                new AuthenticationServiceException("Authentication failed")
        );

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never())
                .generateAccessToken(anyString(), anyString());
    }

    @Test
    void login_shouldRejectInactiveUser() {

        LoginRequest request =
                new LoginRequest(
                        "john",
                        "password123"
                );

        User user = User.builder()
                .id(1L)
                .username("john")
                .email("john@gmail.com")
                .password("encoded-password")
                .role(Role.USER)
                .active(false)
                .build();

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(mock(
                org.springframework.security.core.Authentication.class
        ));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserAccountInactiveException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never())
                .generateAccessToken(anyString(), anyString());
    }

    // ---------------------------------------------------------
    // REFRESH TOKEN
    // ---------------------------------------------------------

    @Test
    void generateRefreshToken_shouldDelegateToJwtService() {

        when(jwtService.generateRefreshToken("john"))
                .thenReturn("refresh-token");

        String result =
                authService.generateRefreshToken("john");

        assertEquals("refresh-token", result);

        verify(jwtService)
                .generateRefreshToken("john");
    }

    @Test
    void refresh_shouldReturnNewAccessToken() {

        User user = User.builder()
                .id(1L)
                .username("john")
                .role(Role.USER)
                .active(true)
                .build();

        when(jwtService.isRefreshToken("refresh-token"))
                .thenReturn(true);

        when(jwtService.extractUsername("refresh-token"))
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(jwtService.isTokenValid("refresh-token", "john"))
                .thenReturn(true);

        when(jwtService.generateAccessToken("john", "USER"))
                .thenReturn("new-access-token");

        AuthResponse response =
                authService.refresh("refresh-token");

        assertEquals("new-access-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("john", response.username());
        assertEquals("USER", response.role());
    }

    @Test
    void refresh_shouldRejectMissingToken() {

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> authService.refresh(null)
        );

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> authService.refresh("")
        );
    }

    @Test
    void refresh_shouldRejectAccessTokenUsedAsRefreshToken() {

        when(jwtService.isRefreshToken("access-token"))
                .thenReturn(false);

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> authService.refresh("access-token")
        );

        verify(jwtService, never())
                .extractUsername(anyString());
    }

    @Test
    void refresh_shouldRejectInactiveUser() {

        User user = User.builder()
                .id(1L)
                .username("john")
                .role(Role.USER)
                .active(false)
                .build();

        when(jwtService.isRefreshToken("refresh-token"))
                .thenReturn(true);

        when(jwtService.extractUsername("refresh-token"))
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserAccountInactiveException.class,
                () -> authService.refresh("refresh-token")
        );
    }

    @Test
    void refresh_shouldRejectInvalidToken() {

        User user = User.builder()
                .id(1L)
                .username("john")
                .role(Role.USER)
                .active(true)
                .build();

        when(jwtService.isRefreshToken("refresh-token"))
                .thenReturn(true);

        when(jwtService.extractUsername("refresh-token"))
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(jwtService.isTokenValid("refresh-token", "john"))
                .thenReturn(false);

        assertThrows(
                InvalidRefreshTokenException.class,
                () -> authService.refresh("refresh-token")
        );
    }
}