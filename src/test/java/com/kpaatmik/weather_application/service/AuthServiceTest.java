package com.kpaatmik.weather_application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kpaatmik.weather_application.audit.*;
import com.kpaatmik.weather_application.dto.request.*;
import com.kpaatmik.weather_application.dto.response.AuthResponse;
import com.kpaatmik.weather_application.entity.*;
import com.kpaatmik.weather_application.exception.*;
import com.kpaatmik.weather_application.repository.UserRepository;
import com.kpaatmik.weather_application.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock AuditService auditService;

    @InjectMocks AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("aatmik", "aatmik@example.com", "password123");
        loginRequest = new LoginRequest("aatmik", "password123");
        user = User.builder()
                .id(1L).username("aatmik").email("aatmik@example.com")
                .password("HASH").role(Role.USER).active(true).build();
    }

    @Test
    void register_shouldCreateUserWithHashedPasswordAndUserRole() {
        when(userRepository.existsByUsername("aatmik")).thenReturn(false);
        when(userRepository.existsByEmail("aatmik@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.register(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("aatmik", saved.getUsername());
        assertEquals("aatmik@example.com", saved.getEmail());
        assertEquals("HASH", saved.getPassword());
        assertEquals(Role.USER, saved.getRole());
        assertTrue(saved.getActive());

        verify(auditService).log(1L, AuditAction.USER_REGISTERED, AuditEntityType.USER,
                1L, "User registered successfully");
    }

    @Test
    void register_shouldRejectDuplicateUsername() {
        when(userRepository.existsByUsername("aatmik")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verifyNoInteractions(auditService);
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        when(userRepository.existsByUsername("aatmik")).thenReturn(false);
        when(userRepository.existsByEmail("aatmik@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(auditService);
    }

    @Test
    void login_shouldReturnAccessTokenAndAudit() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userRepository.findByUsername("aatmik")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("aatmik", "USER")).thenReturn("access-token");

        AuthResponse response = authService.login(loginRequest);

        assertEquals("access-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("aatmik", response.username());
        assertEquals("USER", response.role());

        verify(auditService).log(1L, AuditAction.USER_LOGIN, AuditEntityType.USER,
                1L, "User logged in successfully");
    }

    @Test
    void login_shouldTranslateAuthenticationFailure() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));

        verify(userRepository, never()).findByUsername(anyString());
        verifyNoInteractions(jwtService);
        verifyNoInteractions(auditService);
    }

    @Test
    void login_shouldRejectMissingUserAfterSuccessfulAuthentication() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userRepository.findByUsername("aatmik")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    void login_shouldRejectInactiveUser() {
        user.setActive(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userRepository.findByUsername("aatmik")).thenReturn(Optional.of(user));

        assertThrows(UserAccountInactiveException.class,
                () -> authService.login(loginRequest));

        verifyNoInteractions(jwtService);
        verifyNoInteractions(auditService);
    }

    @Test
    void generateRefreshToken_shouldDelegateToJwtService() {
        when(jwtService.generateRefreshToken("aatmik")).thenReturn("refresh-token");

        assertEquals("refresh-token", authService.generateRefreshToken("aatmik"));
    }

    @Test
    void refresh_shouldRejectNullToken() {
        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refresh(null));
        verifyNoInteractions(userRepository, jwtService);
    }

    @Test
    void refresh_shouldRejectBlankToken() {
        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refresh("   "));
    }

    @Test
    void refresh_shouldRejectAccessTokenUsedAsRefreshToken() {
        when(jwtService.isRefreshToken("access-token")).thenReturn(false);

        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refresh("access-token"));

        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void refresh_shouldRejectMalformedRefreshToken() {
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("refresh-token"))
                .thenThrow(new RuntimeException("malformed"));

        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refresh("refresh-token"));
    }

    @Test
    void refresh_shouldRejectUnknownUser() {
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("refresh-token")).thenReturn("unknown");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refresh("refresh-token"));
    }

    @Test
    void refresh_shouldRejectInactiveUser() {
        user.setActive(false);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("refresh-token")).thenReturn("aatmik");
        when(userRepository.findByUsername("aatmik")).thenReturn(Optional.of(user));

        assertThrows(UserAccountInactiveException.class,
                () -> authService.refresh("refresh-token"));
    }

    @Test
    void refresh_shouldRejectExpiredOrInvalidToken() {
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("refresh-token")).thenReturn("aatmik");
        when(userRepository.findByUsername("aatmik")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("refresh-token", "aatmik")).thenReturn(false);

        assertThrows(InvalidRefreshTokenException.class,
                () -> authService.refresh("refresh-token"));

        verify(jwtService, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    void refresh_shouldReturnNewAccessToken() {
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("refresh-token")).thenReturn("aatmik");
        when(userRepository.findByUsername("aatmik")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("refresh-token", "aatmik")).thenReturn(true);
        when(jwtService.generateAccessToken("aatmik", "USER")).thenReturn("new-access-token");

        AuthResponse response = authService.refresh("refresh-token");

        assertEquals("new-access-token", response.accessToken());
        assertEquals("USER", response.role());
        verify(jwtService).generateAccessToken("aatmik", "USER");
    }
}
