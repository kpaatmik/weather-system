package com.kpaatmik.weatherapplication.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

	@Autowired
	private MockMvc mockMvc;

	// ============================================================
	// PUBLIC ENDPOINTS
	// ============================================================

	@Test
	void register_shouldBePublic() throws Exception {

		mockMvc.perform(post("/api/auth/register")).andExpect(
				result -> org.junit.jupiter.api.Assertions.assertNotEquals(401, result.getResponse().getStatus()));
	}

	@Test
	void login_shouldBePublic() throws Exception {

		mockMvc.perform(post("/api/auth/login")).andExpect(
				result -> org.junit.jupiter.api.Assertions.assertNotEquals(401, result.getResponse().getStatus()));
	}

	@Test
	void logout_shouldBePublic() throws Exception {

		mockMvc.perform(post("/api/auth/logout")).andExpect(
				result -> org.junit.jupiter.api.Assertions.assertNotEquals(401, result.getResponse().getStatus()));
	}

	// ============================================================
	// ADMIN ENDPOINTS
	// ============================================================

	@Test
	void adminEndpoint_withoutAuthentication_shouldReturn401() throws Exception {

		mockMvc.perform(get("/api/admin/cities")).andExpect(status().isUnauthorized());
	}

	@Test
	void adminEndpoint_withUserRole_shouldReturn403() throws Exception {

		mockMvc.perform(get("/api/admin/cities").with(user("normalUser").roles("USER")))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminEndpoint_withAdminRole_shouldNotReturn403() throws Exception {

		mockMvc.perform(get("/api/admin/cities").with(user("admin").roles("ADMIN"))).andExpect(
				result -> org.junit.jupiter.api.Assertions.assertNotEquals(403, result.getResponse().getStatus()));
	}

	// ============================================================
	// CITY ENDPOINTS
	// ============================================================

	@Test
	void cityEndpoint_withoutAuthentication_shouldReturn401() throws Exception {

		mockMvc.perform(get("/api/cities/active")).andExpect(status().isUnauthorized());
	}

	@Test
	void cityEndpoint_withUserRole_shouldBeAllowed() throws Exception {

		mockMvc.perform(get("/api/cities/active").with(user("user").roles("USER"))).andExpect(
				result -> org.junit.jupiter.api.Assertions.assertNotEquals(403, result.getResponse().getStatus()));
	}

	@Test
	void cityEndpoint_withAdminRole_shouldBeAllowed() throws Exception {

		mockMvc.perform(get("/api/cities/active").with(user("admin").roles("ADMIN"))).andExpect(
				result -> org.junit.jupiter.api.Assertions.assertNotEquals(403, result.getResponse().getStatus()));
	}

	@Test
	void weatherEndpoint_withoutAuthentication_shouldReturn401() throws Exception {

		mockMvc.perform(get("/api/cities/weather/1")).andExpect(status().isUnauthorized());
	}

	@Test
	void weatherEndpoint_withUserRole_shouldBeAllowed() throws Exception {

		mockMvc.perform(get("/api/cities/weather/1").with(user("user").roles("USER"))).andExpect(
				result -> org.junit.jupiter.api.Assertions.assertNotEquals(403, result.getResponse().getStatus()));
	}

	@Test
	void weatherEndpoint_withAdminRole_shouldBeAllowed() throws Exception {

		mockMvc.perform(get("/api/cities/weather/1").with(user("admin").roles("ADMIN"))).andExpect(
				result -> org.junit.jupiter.api.Assertions.assertNotEquals(403, result.getResponse().getStatus()));
	}

	// ============================================================
	// DEFAULT RULE
	// ============================================================

	@Test
	void unknownEndpoint_withoutAuthentication_shouldReturn401() throws Exception {

		mockMvc.perform(get("/api/does-not-exist")).andExpect(status().isUnauthorized());
	}
}