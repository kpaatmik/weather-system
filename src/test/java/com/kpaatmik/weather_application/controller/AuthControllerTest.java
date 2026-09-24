package com.kpaatmik.weather_application.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kpaatmik.weather_application.dto.response.AuthResponse;
import com.kpaatmik.weather_application.service.AuditService;
import com.kpaatmik.weather_application.service.AuthService;
import com.kpaatmik.weather_application.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.Mockito;

class AuthControllerTest {

    private MockMvc mockMvc;

    private AuthService authService;
    private AuditService auditService;
    private UserService userService;

    @BeforeEach
    void setUp() {

        authService = Mockito.mock(AuthService.class);
        auditService = Mockito.mock(AuditService.class);
        userService = Mockito.mock(UserService.class);

        AuthController controller =
                new AuthController(
                        authService,
                        auditService,
                        userService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------

    @Test
    void register_shouldReturnSuccess() throws Exception {

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "john",
                                    "email": "john@gmail.com",
                                    "password": "password123"
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(content()
                .string("User registered successfully"));

        verify(authService).register(any());
    }

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------

    @Test
    void login_shouldReturnAccessTokenAndRefreshCookie()
            throws Exception {

        AuthResponse response =
                new AuthResponse(
                        "access-token",
                        "Bearer",
                        "john",
                        "USER"
                );

        when(authService.login(any()))
                .thenReturn(response);

        when(authService.generateRefreshToken("john"))
                .thenReturn("refresh-token");

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "john",
                                    "password": "password123"
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken")
                .value("access-token"))
        .andExpect(jsonPath("$.tokenType")
                .value("Bearer"))
        .andExpect(jsonPath("$.username")
                .value("john"))
        .andExpect(jsonPath("$.role")
                .value("USER"))
        .andExpect(header().string(
                "Set-Cookie",
                org.hamcrest.Matchers.containsString(
                        "refresh_token=refresh-token"
                )
        ))
        .andExpect(header().string(
                "Set-Cookie",
                org.hamcrest.Matchers.containsString(
                        "HttpOnly"
                )
        ))
        .andExpect(header().string(
                "Set-Cookie",
                org.hamcrest.Matchers.containsString(
                        "SameSite=Strict"
                )
        ));

        verify(authService)
                .generateRefreshToken("john");
    }

    // ---------------------------------------------------------
    // REFRESH
    // ---------------------------------------------------------

    @Test
    void refresh_withCookie_shouldReturnNewAccessToken()
            throws Exception {

        AuthResponse response =
                new AuthResponse(
                        "new-access-token",
                        "Bearer",
                        "john",
                        "USER"
                );

        when(authService.refresh("refresh-token"))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/auth/refresh")
                        .cookie(
                                new jakarta.servlet.http.Cookie(
                                        "refresh_token",
                                        "refresh-token"
                                )
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken")
                .value("new-access-token"))
        .andExpect(jsonPath("$.username")
                .value("john"));
    }

  

    // ---------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------

    @Test
    void logout_shouldClearRefreshCookie()
            throws Exception {

        when(userService.getUserId(any()))
                .thenReturn(1L);

        mockMvc.perform(
                post("/api/auth/logout")
        )
        .andExpect(status().isOk())
        .andExpect(content()
                .string("Logged out successfully"))
        .andExpect(header().string(
                "Set-Cookie",
                org.hamcrest.Matchers.containsString(
                        "refresh_token="
                )
        ))
        .andExpect(header().string(
                "Set-Cookie",
                org.hamcrest.Matchers.containsString(
                        "Max-Age=0"
                )
        ));
    }
}