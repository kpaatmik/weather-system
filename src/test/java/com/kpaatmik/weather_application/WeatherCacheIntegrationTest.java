package com.kpaatmik.weather_application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import com.kpaatmik.weather_application.client.OpenWeatherClient;
import com.kpaatmik.weather_application.dto.request.CreateCityRequest;
import com.kpaatmik.weather_application.dto.response.OpenWeatherResponse;
import com.kpaatmik.weather_application.dto.response.WeatherResponse;
import com.kpaatmik.weather_application.entity.City;
import com.kpaatmik.weather_application.repository.CityRepository;
import com.kpaatmik.weather_application.service.WeatherService;
import com.kpaatmik.weather_application.service.CityService;
import com.kpaatmik.weather_application.service.AuditService;
import com.kpaatmik.weather_application.service.UserService;

@SpringBootTest
@ActiveProfiles("test")
class WeatherCacheIntegrationTest {

    @Autowired WeatherService weatherService;
    @Autowired CityService cityService;
    @Autowired CityRepository cityRepository;
    @Autowired CacheManager cacheManager;

    @MockitoBean OpenWeatherClient weatherClient;
    @MockitoBean AuditService auditService;
    @MockitoBean UserService userService;

    private City city;
    private OpenWeatherResponse providerResponse;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("weather").clear();
        cityRepository.deleteAll();

        city = cityRepository.saveAndFlush(City.builder()
                .name("KANNUR").country("IN")
                .latitude(11.8745).longitude(75.3704).active(true).build());

        providerResponse = new OpenWeatherResponse(
                new OpenWeatherResponse.Main(30.0, 31.0, 70),
                new OpenWeatherResponse.Wind(3.0),
                List.of(new OpenWeatherResponse.Weather("Clouds", "cloudy", "03d")));

        when(weatherClient.getWeather(anyDouble(), anyDouble()))
                .thenReturn(providerResponse);
        when(userService.getUserId(anyString())).thenReturn(1L);
    }

    @Test
    void repeatedWeatherRequest_shouldUseCacheAndCallProviderOnce() {
        WeatherResponse first = weatherService.getWeather(city.getId());
        WeatherResponse second = weatherService.getWeather(city.getId());

        assertEquals(first, second);
        verify(weatherClient, times(1)).getWeather(11.8745, 75.3704);
    }

    @Test
    void cacheShouldBeClearedWhenCityIsDeactivated() {
        weatherService.getWeather(city.getId());
        verify(weatherClient, times(1)).getWeather(anyDouble(), anyDouble());

        cityService.deactivateCity(city.getId());

        assertThrows(Exception.class, () -> weatherService.getWeather(city.getId()));
        verify(weatherClient, times(1)).getWeather(anyDouble(), anyDouble());
    }
}
