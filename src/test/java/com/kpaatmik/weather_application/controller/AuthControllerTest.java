package com.kpaatmik.weather_application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpaatmik.weather_application.dto.request.LoginRequest;
import com.kpaatmik.weather_application.dto.request.RegisterRequest;
import com.kpaatmik.weather_application.dto.response.AuthResponse;
import com.kpaatmik.weather_application.service.AuditService;
import com.kpaatmik.weather_application.service.AuthService;
import com.kpaatmik.weather_application.service.UserService;

import jakarta.servlet.http.Cookie;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;

	@MockitoBean
	AuthService authService;
	@MockitoBean
	AuditService auditService;
	@MockitoBean
	UserService userService;

	@Test
	void register_shouldReturn200() throws Exception {
		var request = new RegisterRequest("aatmik", "aatmik@example.com", "password123");

		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(content().string("User registered successfully"));

		verify(authService).register(any(RegisterRequest.class));
	}

	@Test
	void register_shouldRejectInvalidPayload() throws Exception {
		var request = new RegisterRequest("a", "bad-email", "123");

		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

		verify(authService, never()).register(any());
	}

	@Test
	void login_shouldReturnTokenAndRefreshCookie() throws Exception {
		var request = new LoginRequest("aatmik", "password123");
		var response = new AuthResponse("access", "Bearer", "aatmik", "USER");

		when(authService.login(any(LoginRequest.class))).thenReturn(response);
		when(authService.generateRefreshToken("aatmik")).thenReturn("refresh-token");

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value("access")).andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(header().string("Set-Cookie",
						org.hamcrest.Matchers.containsString("refresh_token=refresh-token")))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("SameSite=Strict")));
	}

	@Test
	void refresh_withoutCookie_shouldStillDelegateNullAndReturnServiceError() throws Exception {
		when(authService.refresh(null))
				.thenThrow(new com.kpaatmik.weather_application.exception.InvalidRefreshTokenException(
						"Refresh token is missing"));

		mockMvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
	}

	@Test
	void refresh_withCookie_shouldReturnNewToken() throws Exception {
		when(authService.refresh("refresh-token"))
				.thenReturn(new AuthResponse("new-access", "Bearer", "aatmik", "USER"));

		mockMvc.perform(post("/api/auth/refresh").cookie(new Cookie("refresh_token", "refresh-token")))
				.andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("new-access"));
	}

	@Test
	void logout_shouldClearRefreshCookieAndAudit() throws Exception {
		when(userService.getUserId(isNull())).thenReturn(1L);

		mockMvc.perform(post("/api/auth/logout")).andExpect(status().isOk())
				.andExpect(content().string("Logged out successfully"))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

		verify(auditService).log(eq(1L), eq(com.kpaatmik.weather_application.audit.AuditAction.USER_LOGOUT),
				eq(com.kpaatmik.weather_application.audit.AuditEntityType.USER), eq(1L), contains("loged out"));
	}
}
