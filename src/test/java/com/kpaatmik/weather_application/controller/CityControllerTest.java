package com.kpaatmik.weather_application.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.kpaatmik.weather_application.dto.request.CreateCityRequest;
import com.kpaatmik.weather_application.dto.response.*;
import com.kpaatmik.weather_application.service.CityService;

@WebMvcTest(CityController.class)
@AutoConfigureMockMvc(addFilters = false)
class CityControllerTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;
	@MockitoBean
	CityService cityService;

	@Test
	void getAllCities_shouldReturn200() throws Exception {
		when(cityService.getAllCities()).thenReturn(List.of());

		mockMvc.perform(get("/api/admin/cities")).andExpect(status().isOk()).andExpect(content().json("[]"));
	}

	@Test
	void deleteCity_shouldReturn204() throws Exception {
		mockMvc.perform(delete("/api/admin/cities/1")).andExpect(status().isNoContent());

		verify(cityService).deleteCity(1L);
	}

	@Test
	void deactivateCity_shouldReturnUpdatedCity() throws Exception {
		CityResponse response = new CityResponse(1L, "KANNUR", "KERALA", "IN", 11.8745, 75.3704, false, null, null);
		when(cityService.deactivateCity(1L)).thenReturn(response);

		mockMvc.perform(patch("/api/admin/cities/1/deactivate")).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("KANNUR")).andExpect(jsonPath("$.active").value(false));
	}

	@Test
	void searchCities_shouldReturnSuggestions() throws Exception {
		when(cityService.searchCities("Kan"))
				.thenReturn(List.of(new CitySuggestionResponse("Kannur", "Kerala", "IN", 11.8745, 75.3704)));

		mockMvc.perform(get("/api/admin/cities/sugesstion").param("q", "Kan")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Kannur")).andExpect(jsonPath("$[0].longitude").value(75.3704));
	}

	@Test
	void createCity_shouldReturn201() throws Exception {
		var request = new CreateCityRequest("Kannur", "Kerala", "IN", 11.8745, 75.3704);
		var response = new CityResponse(1L, "KANNUR", "KERALA", "IN", 11.8745, 75.3704, true, null, null);

		when(cityService.createCity(any(CreateCityRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/admin/cities/save").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.active").value(true));
	}

	@Test
	void createCity_shouldRejectInvalidPayload() throws Exception {
		var request = new CreateCityRequest("", "Kerala", "IN", 100.0, 200.0);

		mockMvc.perform(post("/api/admin/cities/save").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

		verify(cityService, never()).createCity(any());
	}
}
