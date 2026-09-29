package com.kpaatmik.weatherapplication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.kpaatmik.weatherapplication.client.OpenWeatherClient;
import com.kpaatmik.weatherapplication.dto.response.OpenWeatherResponse;
import com.kpaatmik.weatherapplication.dto.response.WeatherResponse;
import com.kpaatmik.weatherapplication.entity.City;
import com.kpaatmik.weatherapplication.exception.CityNotFoundException;
import com.kpaatmik.weatherapplication.exception.WeatherServiceException;
import com.kpaatmik.weatherapplication.repository.CityRepository;

class WeatherCacheServiceTest {

    private CityRepository cityRepository;
    private OpenWeatherClient weatherClient;
    private WeatherCacheService weatherCacheService;

    @BeforeEach
    void setUp() {

        cityRepository = mock(CityRepository.class);
        weatherClient = mock(OpenWeatherClient.class);

        weatherCacheService =
                new WeatherCacheService(
                        cityRepository,
                        weatherClient
                );
    }

    // ---------------------------------------------------------
    // SUCCESSFUL WEATHER RETRIEVAL
    // ---------------------------------------------------------

    @Test
    void getWeather_shouldReturnWeatherResponse_whenCityIsActive() {

        // Arrange
        City city = createCity(
                1L,
                "Kannur",
                true,
                11.8745,
                75.3704
        );

        OpenWeatherResponse weather =
                createWeatherResponse(
                        30.5,
                        32.0,
                        75,
                        4.2,
                        "Clouds",
                        "overcast clouds"
                );

        when(cityRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(weatherClient.getWeather(
                11.8745,
                75.3704
        )).thenReturn(weather);

        // Act
        WeatherResponse response =
                weatherCacheService.getWeather(1L);

        // Assert
        assertNotNull(response);

        assertEquals(1L, response.cityId());
        assertEquals("Kannur", response.cityName());

        assertEquals(30.5, response.temperature());
        assertEquals(32.0, response.feelsLike());
        assertEquals(75, response.humidity());
        assertEquals(4.2, response.windSpeed());

        assertEquals("Clouds", response.condition());
        assertEquals("overcast clouds", response.description());

        verify(cityRepository).findById(1L);

        verify(weatherClient).getWeather(
                11.8745,
                75.3704
        );
    }


    // ---------------------------------------------------------
    // CITY NOT FOUND
    // ---------------------------------------------------------

    @Test
    void getWeather_shouldThrowCityNotFoundException_whenCityDoesNotExist() {

        // Arrange
        when(cityRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                CityNotFoundException.class,
                () -> weatherCacheService.getWeather(99L)
        );

        verify(cityRepository).findById(99L);

        // OpenWeather should never be called
        verifyNoInteractions(weatherClient);
    }


    // ---------------------------------------------------------
    // INACTIVE CITY
    // ---------------------------------------------------------

    @Test
    void getWeather_shouldThrowCityNotFoundException_whenCityIsInactive() {

        // Arrange
        City city = createCity(
                2L,
                "Kochi",
                false,
                9.9312,
                76.2673
        );

        when(cityRepository.findById(2L))
                .thenReturn(Optional.of(city));

        // Act & Assert
        assertThrows(
                CityNotFoundException.class,
                () -> weatherCacheService.getWeather(2L)
        );

        verify(cityRepository).findById(2L);

        // External API should not be called
        verifyNoInteractions(weatherClient);
    }


    // ---------------------------------------------------------
    // EXTERNAL API FAILURE
    // ---------------------------------------------------------

    @Test
    void getWeather_shouldPropagateWeatherServiceException_whenWeatherClientFails() {

        // Arrange
        City city = createCity(
                3L,
                "Bangalore",
                true,
                12.9716,
                77.5946
        );

        when(cityRepository.findById(3L))
                .thenReturn(Optional.of(city));

        WeatherServiceException exception =
                new WeatherServiceException(
                        "Unable to connect to OpenWeather API",
                        new RuntimeException("Connection timeout")
                );

        when(weatherClient.getWeather(
                12.9716,
                77.5946
        )).thenThrow(exception);

        // Act & Assert
        WeatherServiceException thrown =
                assertThrows(
                        WeatherServiceException.class,
                        () -> weatherCacheService.getWeather(3L)
                );

        assertEquals(
                "Unable to connect to OpenWeather API",
                thrown.getMessage()
        );

        verify(cityRepository).findById(3L);

        verify(weatherClient).getWeather(
                12.9716,
                77.5946
        );
    }


    // ---------------------------------------------------------
    // WEATHER MAPPING
    // ---------------------------------------------------------

    @Test
    void getWeather_shouldMapOpenWeatherResponseCorrectly() {

        // Arrange
        City city = createCity(
                4L,
                "Mumbai",
                true,
                19.0760,
                72.8777
        );

        OpenWeatherResponse weather =
                createWeatherResponse(
                        28.75,
                        30.10,
                        82,
                        6.50,
                        "Rain",
                        "light rain"
                );

        when(cityRepository.findById(4L))
                .thenReturn(Optional.of(city));

        when(weatherClient.getWeather(
                19.0760,
                72.8777
        )).thenReturn(weather);

        // Act
        WeatherResponse response =
                weatherCacheService.getWeather(4L);

        // Assert
        assertEquals(4L, response.cityId());
        assertEquals("Mumbai", response.cityName());

        assertEquals(28.75, response.temperature());
        assertEquals(30.10, response.feelsLike());
        assertEquals(82, response.humidity());
        assertEquals(6.50, response.windSpeed());

        assertEquals("Rain", response.condition());
        assertEquals("light rain", response.description());
    }


    // ---------------------------------------------------------
    // VERIFY COORDINATES
    // ---------------------------------------------------------

    @Test
    void getWeather_shouldUseCityCoordinates_whenCallingWeatherClient() {

        // Arrange
        City city = createCity(
                5L,
                "Delhi",
                true,
                28.6139,
                77.2090
        );

        OpenWeatherResponse weather =
                createWeatherResponse(
                        25.0,
                        26.0,
                        60,
                        3.0,
                        "Clear",
                        "clear sky"
                );

        when(cityRepository.findById(5L))
                .thenReturn(Optional.of(city));

        when(weatherClient.getWeather(
                28.6139,
                77.2090
        )).thenReturn(weather);

        // Act
        weatherCacheService.getWeather(5L);

        // Assert
        verify(weatherClient).getWeather(
                28.6139,
                77.2090
        );
    }


    // ---------------------------------------------------------
    // VERIFY API IS NOT CALLED FOR INVALID CITY
    // ---------------------------------------------------------

    @Test
    void getWeather_shouldNotCallWeatherClient_whenCityDoesNotExist() {

        // Arrange
        when(cityRepository.findById(100L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                CityNotFoundException.class,
                () -> weatherCacheService.getWeather(100L)
        );

        verifyNoInteractions(weatherClient);
    }


    // ---------------------------------------------------------
    // VERIFY API IS NOT CALLED FOR INACTIVE CITY
    // ---------------------------------------------------------

    @Test
    void getWeather_shouldNotCallWeatherClient_whenCityIsInactive() {

        // Arrange
        City city = createCity(
                6L,
                "Chennai",
                false,
                13.0827,
                80.2707
        );

        when(cityRepository.findById(6L))
                .thenReturn(Optional.of(city));

        // Act & Assert
        assertThrows(
                CityNotFoundException.class,
                () -> weatherCacheService.getWeather(6L)
        );

        verify(weatherClient, never())
                .getWeather(anyDouble(), anyDouble());
    }


    // ---------------------------------------------------------
    // HELPER: CREATE CITY
    // ---------------------------------------------------------

    private City createCity(
            Long id,
            String name,
            Boolean active,
            Double latitude,
            Double longitude
    ) {

        City city = new City();

        city.setId(id);
        city.setName(name);
        city.setActive(active);
        city.setLatitude(latitude);
        city.setLongitude(longitude);

        return city;
    }


    // ---------------------------------------------------------
    // HELPER: CREATE OPENWEATHER RESPONSE
    // ---------------------------------------------------------

    private OpenWeatherResponse createWeatherResponse(
            Double temperature,
            Double feelsLike,
            Integer humidity,
            Double windSpeed,
            String condition,
            String description
    ) {

        OpenWeatherResponse.Main main =
                new OpenWeatherResponse.Main(
                        temperature,
                        feelsLike,
                        humidity
                );

        OpenWeatherResponse.Wind wind =
                new OpenWeatherResponse.Wind(
                        windSpeed
                );

        OpenWeatherResponse.Weather currentWeather =
                new OpenWeatherResponse.Weather(
                        condition,
                        description,
                        "01d"
                );

        return new OpenWeatherResponse(
                main,
                wind,
                List.of(currentWeather)
        );
    }
}