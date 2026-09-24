package com.kpaatmik.weather_application;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kpaatmik.weather_application.client.OpenWeatherClient;
import com.kpaatmik.weather_application.client.OpenWeatherGeocodingClient;
import com.kpaatmik.weather_application.entity.Role;
import com.kpaatmik.weather_application.entity.User;
import com.kpaatmik.weather_application.repository.AuditLogRepository;
import com.kpaatmik.weather_application.repository.CityRepository;
import com.kpaatmik.weather_application.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	UserRepository userRepository;
	@Autowired
	CityRepository cityRepository;
	@Autowired
	AuditLogRepository auditLogRepository;

	@MockitoBean
	OpenWeatherClient openWeatherClient;
	@MockitoBean
	OpenWeatherGeocodingClient geocodingClient;

	@BeforeEach
	void clean() {
		cityRepository.deleteAll();
		auditLogRepository.deleteAll();
		userRepository.deleteAll();

		userRepository.save(User.builder().username("user").email("user@example.com").password("{noop}password")
				.role(Role.USER).active(true).build());

		userRepository.save(User.builder().username("admin").email("admin@example.com").password("{noop}password")
				.role(Role.ADMIN).active(true).build());
	}

	@Test
	void unauthenticatedProtectedEndpoint_shouldReturn401() throws Exception {
		mockMvc.perform(get("/api/cities/active")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void userShouldAccessUserEndpoint() throws Exception {
		mockMvc.perform(get("/api/cities/active").with(user("user").roles("USER"))).andExpect(status().isOk());
	}

	@Test
	void userShouldNotAccessAdminEndpoint() throws Exception {
		mockMvc.perform(get("/api/admin/cities").with(user("user").roles("USER"))).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	void adminShouldAccessAdminEndpoint() throws Exception {
		mockMvc.perform(get("/api/admin/cities").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
	}

	@Test
	void swaggerShouldBePublic() throws Exception {
		mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
	}

	@Test
	void invalidBearerTokenShouldReturn401ForProtectedEndpoint() throws Exception {
		mockMvc.perform(get("/api/cities/active").header("Authorization", "Bearer definitely-invalid-token"))
				.andExpect(status().isUnauthorized());
	}
}
