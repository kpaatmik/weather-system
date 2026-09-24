package com.kpaatmik.weather_application.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.kpaatmik.weather_application.dto.response.PublicCityResponse;
import com.kpaatmik.weather_application.dto.response.WeatherResponse;
import com.kpaatmik.weather_application.service.CityService;
import com.kpaatmik.weather_application.service.WeatherService;

@WebMvcTest(WeatherController.class)
@AutoConfigureMockMvc(addFilters = false)
class WeatherControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean CityService cityService;
    @MockitoBean WeatherService weatherService;

    @Test
    void getWeather_shouldReturnWeather() throws Exception {
        var response = new WeatherResponse(1L, "KANNUR", 30.5, 31.0,
                78, 4.2, "Clouds", "broken clouds");

        when(weatherService.getWeather(1L)).thenReturn(response);

        mockMvc.perform(get("/api/cities/weather/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cityId").value(1))
                .andExpect(jsonPath("$.temperature").value(30.5))
                .andExpect(jsonPath("$.condition").value("Clouds"));
    }

    @Test
    void getActiveCities_shouldReturnList() throws Exception {
        when(cityService.getActiveCities()).thenReturn(
                List.of(new PublicCityResponse(1L, "KANNUR", "KERALA", "IN")));

        mockMvc.perform(get("/api/cities/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("KANNUR"));
    }

    @Test
    void getWeather_shouldRejectMalformedPathVariable() throws Exception {
        mockMvc.perform(get("/api/cities/weather/not-a-number"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(weatherService);
    }
}
