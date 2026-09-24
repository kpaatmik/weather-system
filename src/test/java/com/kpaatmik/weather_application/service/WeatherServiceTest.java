package com.kpaatmik.weather_application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kpaatmik.weather_application.audit.*;
import com.kpaatmik.weather_application.client.OpenWeatherClient;
import com.kpaatmik.weather_application.dto.response.OpenWeatherResponse;
import com.kpaatmik.weather_application.dto.response.WeatherResponse;
import com.kpaatmik.weather_application.entity.City;
import com.kpaatmik.weather_application.exception.CityNotFoundException;
import com.kpaatmik.weather_application.repository.CityRepository;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock CityRepository cityRepository;
    @Mock OpenWeatherClient weatherClient;
    @Mock AuditService auditService;
    @Mock UserService userService;

    @InjectMocks WeatherService weatherService;

    private City city;
    private OpenWeatherResponse weather;

    @BeforeEach
    void setUp() {
        city = City.builder().id(1L).name("KANNUR").country("IN")
                .latitude(11.8745).longitude(75.3704).active(true).build();

        weather = new OpenWeatherResponse(
                new OpenWeatherResponse.Main(30.5, 31.0, 78),
                new OpenWeatherResponse.Wind(4.2),
                List.of(new OpenWeatherResponse.Weather("Clouds", "broken clouds", "04d")));
    }

    @Test
    void getWeather_shouldReturnMappedWeatherForActiveCity() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(weatherClient.getWeather(11.8745, 75.3704)).thenReturn(weather);
        
        WeatherResponse response = weatherService.getWeather(1L);

        assertEquals(1L, response.cityId());
        assertEquals("KANNUR", response.cityName());
        assertEquals(30.5, response.temperature());
        assertEquals(31.0, response.feelsLike());
        assertEquals(78, response.humidity());
        assertEquals(4.2, response.windSpeed());
        assertEquals("Clouds", response.condition());
        assertEquals("broken clouds", response.description());

        verify(weatherClient).getWeather(11.8745, 75.3704);
        verify(auditService).log(null, AuditAction.WEATHER_SEARCHED,
                AuditEntityType.WEATHER, null, "Weather Searched: 1");
    }

    @Test
    void getWeather_shouldRejectUnknownCityWithoutCallingProvider() {
        when(cityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CityNotFoundException.class,
                () -> weatherService.getWeather(999L));

        verifyNoInteractions(weatherClient);
    }

    @Test
    void getWeather_shouldRejectInactiveCityWithoutCallingProvider() {
        city.setActive(false);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));

        assertThrows(CityNotFoundException.class,
                () -> weatherService.getWeather(1L));

        verifyNoInteractions(weatherClient);
    }

    @Test
    void getWeather_shouldPropagateProviderFailure() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(weatherClient.getWeather(anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("OpenWeather unavailable"));

        assertThrows(RuntimeException.class,
                () -> weatherService.getWeather(1L));
    }

    @Test
    void getWeather_shouldPropagateNullWeatherResponseFailure() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(weatherClient.getWeather(anyDouble(), anyDouble())).thenReturn(null);

        assertThrows(NullPointerException.class,
                () -> weatherService.getWeather(1L));
    }

    @Test
    void getWeather_shouldExposeCurrentWeatherFirstItem() {
        OpenWeatherResponse response = new OpenWeatherResponse(
                new OpenWeatherResponse.Main(25.0, 25.5, 60),
                new OpenWeatherResponse.Wind(2.0),
                List.of(
                    new OpenWeatherResponse.Weather("Rain", "light rain", "10d"),
                    new OpenWeatherResponse.Weather("Clouds", "cloudy", "03d")
                ));

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(weatherClient.getWeather(anyDouble(), anyDouble())).thenReturn(response);

        WeatherResponse result = weatherService.getWeather(1L);

        assertEquals("Rain", result.condition());
        assertEquals("light rain", result.description());
    }

    @Test
    void getWeather_shouldFailWhenProviderReturnsNoWeatherEntries() {
        OpenWeatherResponse response = new OpenWeatherResponse(
                new OpenWeatherResponse.Main(25.0, 25.5, 60),
                new OpenWeatherResponse.Wind(2.0),
                List.of());

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(weatherClient.getWeather(anyDouble(), anyDouble())).thenReturn(response);

        assertThrows(IndexOutOfBoundsException.class,
                () -> weatherService.getWeather(1L));
    }
}
