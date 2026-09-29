package com.kpaatmik.weatherapplication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.kpaatmik.weatherapplication.audit.AuditAction;
import com.kpaatmik.weatherapplication.audit.AuditEntityType;
import com.kpaatmik.weatherapplication.dto.response.WeatherResponse;
import com.kpaatmik.weatherapplication.security.SecurityUtil;

class WeatherServiceTest {

	private AuditService auditService;
	private UserService userService;
	private WeatherCacheService weatherCacheService;

	private WeatherService weatherService;

	@BeforeEach
	void setUp() {

		auditService = mock(AuditService.class);
		userService = mock(UserService.class);
		weatherCacheService = mock(WeatherCacheService.class);

		weatherService = new WeatherService(null, null, auditService, userService, weatherCacheService);
	}

	@Test
	void getWeather_shouldReturnWeatherSuccessfully() {

		WeatherResponse expectedResponse = new WeatherResponse(1L, "KANNUR", 30.5, 32.0, 70, 4.5, "Clouds",
				"broken clouds");

		when(userService.getUserId(any())).thenReturn(null);

		when(weatherCacheService.getWeather(1L)).thenReturn(expectedResponse);

		WeatherResponse response = weatherService.getWeather(1L);

		assertNotNull(response);

		assertEquals(1L, response.cityId());
		assertEquals("KANNUR", response.cityName());
		assertEquals(30.5, response.temperature());
		assertEquals("Clouds", response.condition());

		verify(weatherCacheService).getWeather(1L);

		verify(auditService).log(isNull(), eq(AuditAction.WEATHER_SEARCHED), eq(AuditEntityType.WEATHER), isNull(),
				eq("Weather searched: 1"));
	}

	@Test
	void getWeather_shouldDelegateToWeatherCacheService() {

		WeatherResponse expectedResponse = new WeatherResponse(1L, "KANNUR", 30.5, 32.0, 70, 4.5, "Clouds",
				"broken clouds");

		when(weatherCacheService.getWeather(1L)).thenReturn(expectedResponse);

		WeatherResponse response = weatherService.getWeather(1L);

		assertEquals(expectedResponse, response);

		verify(weatherCacheService).getWeather(1L);
	}

	@Test
	void getWeather_shouldPropagateCacheServiceException() {

		when(weatherCacheService.getWeather(99L)).thenThrow(new RuntimeException("Weather service unavailable"));

		assertThrows(RuntimeException.class, () -> weatherService.getWeather(99L));

		verify(weatherCacheService).getWeather(99L);
	}
}