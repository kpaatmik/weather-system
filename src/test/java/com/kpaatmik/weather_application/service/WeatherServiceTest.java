package com.kpaatmik.weather_application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kpaatmik.weather_application.audit.AuditAction;
import com.kpaatmik.weather_application.audit.AuditEntityType;
import com.kpaatmik.weather_application.client.OpenWeatherClient;
import com.kpaatmik.weather_application.dto.response.OpenWeatherResponse;
import com.kpaatmik.weather_application.dto.response.WeatherResponse;
import com.kpaatmik.weather_application.entity.City;
import com.kpaatmik.weather_application.exception.CityNotFoundException;
import com.kpaatmik.weather_application.repository.CityRepository;

class WeatherServiceTest {

    private CityRepository cityRepository;
    private OpenWeatherClient weatherClient;
    private AuditService auditService;
    private UserService userService;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {

        cityRepository = mock(CityRepository.class);
        weatherClient = mock(OpenWeatherClient.class);
        auditService = mock(AuditService.class);
        userService = mock(UserService.class);

        weatherService = new WeatherService(
                cityRepository,
                weatherClient,
                auditService,
                userService
        );
    }

    @Test
    void getWeather_shouldReturnWeatherSuccessfully() {

        City city = City.builder()
                .id(1L)
                .name("KANNUR")
                .country("IN")
                .latitude(11.8745)
                .longitude(75.3704)
                .active(true)
                .build();

        OpenWeatherResponse weather =
                new OpenWeatherResponse(
                        new OpenWeatherResponse.Main(
                                30.5,
                                32.0,
                                70
                        ),
                        new OpenWeatherResponse.Wind(
                                4.5
                        ),
                        List.of(
                                new OpenWeatherResponse.Weather(
                                        "Clouds",
                                        "broken clouds",
                                        "04d"
                                )
                        )
                );

        when(cityRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(weatherClient.getWeather(
                11.8745,
                75.3704
        )).thenReturn(weather);

        when(userService.getUserId(any()))
                .thenReturn(null);

        WeatherResponse response =
                weatherService.getWeather(1L);

        assertNotNull(response);

        assertEquals(1L, response.cityId());
        assertEquals("KANNUR", response.cityName());
        assertEquals(30.5, response.temperature());
        assertEquals(32.0, response.feelsLike());
        assertEquals(70, response.humidity());
        assertEquals(4.5, response.windSpeed());
        assertEquals("Clouds", response.condition());
        assertEquals("broken clouds", response.description());

        verify(weatherClient).getWeather(
                11.8745,
                75.3704
        );

        verify(auditService).log(
                isNull(),
                eq(AuditAction.WEATHER_SEARCHED),
                eq(AuditEntityType.WEATHER),
                isNull(),
                eq("Weather Searched: 1")
        );
    }

    @Test
    void getWeather_shouldThrowWhenCityDoesNotExist() {

        when(cityRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                CityNotFoundException.class,
                () -> weatherService.getWeather(99L)
        );

        verify(weatherClient, never())
                .getWeather(anyDouble(), anyDouble());
    }

    @Test
    void getWeather_shouldRejectInactiveCity() {

        City city = City.builder()
                .id(1L)
                .name("KANNUR")
                .country("IN")
                .latitude(11.8745)
                .longitude(75.3704)
                .active(false)
                .build();

        when(cityRepository.findById(1L))
                .thenReturn(Optional.of(city));

        assertThrows(
                CityNotFoundException.class,
                () -> weatherService.getWeather(1L)
        );

        verify(weatherClient, never())
                .getWeather(anyDouble(), anyDouble());
    }

    @Test
    void getWeather_shouldPropagateWeatherClientFailure() {

        City city = City.builder()
                .id(1L)
                .name("KANNUR")
                .country("IN")
                .latitude(11.8745)
                .longitude(75.3704)
                .active(true)
                .build();

        when(cityRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(weatherClient.getWeather(
                11.8745,
                75.3704
        )).thenThrow(
                new RuntimeException("OpenWeather unavailable")
        );

        assertThrows(
                RuntimeException.class,
                () -> weatherService.getWeather(1L)
        );
    }

    @Test
    void getWeather_shouldUseFirstWeatherCondition() {

        City city = City.builder()
                .id(1L)
                .name("KANNUR")
                .country("IN")
                .latitude(11.8745)
                .longitude(75.3704)
                .active(true)
                .build();

        OpenWeatherResponse weather =
                new OpenWeatherResponse(
                        new OpenWeatherResponse.Main(
                                28.0,
                                29.0,
                                80
                        ),
                        new OpenWeatherResponse.Wind(2.0),
                        List.of(
                                new OpenWeatherResponse.Weather(
                                        "Rain",
                                        "light rain",
                                        "10d"
                                ),
                                new OpenWeatherResponse.Weather(
                                        "Clouds",
                                        "cloudy",
                                        "03d"
                                )
                        )
                );

        when(cityRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(weatherClient.getWeather(anyDouble(), anyDouble()))
                .thenReturn(weather);

        WeatherResponse response =
                weatherService.getWeather(1L);

        assertEquals("Rain", response.condition());
        assertEquals("light rain", response.description());
    }
}