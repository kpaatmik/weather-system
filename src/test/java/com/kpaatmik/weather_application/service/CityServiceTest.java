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
import com.kpaatmik.weather_application.client.OpenWeatherGeocodingClient;
import com.kpaatmik.weather_application.dto.request.CreateCityRequest;
import com.kpaatmik.weather_application.dto.response.CityResponse;
import com.kpaatmik.weather_application.dto.response.CitySuggestionResponse;
import com.kpaatmik.weather_application.dto.response.OpenWeatherGeocodingResponse;
import com.kpaatmik.weather_application.entity.City;
import com.kpaatmik.weather_application.exception.CityAlreadyExistsException;
import com.kpaatmik.weather_application.exception.CityNotFoundException;
import com.kpaatmik.weather_application.repository.CityRepository;

class CityServiceTest {

    private OpenWeatherGeocodingClient geocodingClient;
    private CityRepository cityRepository;
    private AuditService auditService;
    private UserService userService;

    private CityService cityService;

    @BeforeEach
    void setUp() {

        geocodingClient = mock(OpenWeatherGeocodingClient.class);
        cityRepository = mock(CityRepository.class);
        auditService = mock(AuditService.class);
        userService = mock(UserService.class);

        cityService = new CityService(
                geocodingClient,
                cityRepository,
                auditService,
                userService
        );
    }

    // ---------------------------------------------------------
    // CITY SEARCH
    // ---------------------------------------------------------

    @Test
    void searchCities_shouldMapGeocodingResults() {

        OpenWeatherGeocodingResponse result =
                new OpenWeatherGeocodingResponse(
                        "Kannur",
                        null,
                        11.8745,
                        75.3704,
                        "IN",
                        "Kerala"
                );

        when(geocodingClient.searchCity("Kannur"))
                .thenReturn(new OpenWeatherGeocodingResponse[]{result});

        List<CitySuggestionResponse> response =
                cityService.searchCities("Kannur");

        assertEquals(1, response.size());

        CitySuggestionResponse city = response.get(0);

        assertEquals("Kannur", city.name());
        assertEquals("Kerala", city.state());
        assertEquals("IN", city.country());
        assertEquals(11.8745, city.latitude());
        assertEquals(75.3704, city.longitude());
    }

    @Test
    void searchCities_shouldReturnEmptyListWhenNoResults() {

        when(geocodingClient.searchCity("Unknown"))
                .thenReturn(new OpenWeatherGeocodingResponse[0]);

        List<CitySuggestionResponse> response =
                cityService.searchCities("Unknown");

        assertTrue(response.isEmpty());
    }

    // ---------------------------------------------------------
    // CREATE CITY
    // ---------------------------------------------------------

    @Test
    void createCity_shouldNormalizeAndSaveCity() {

        CreateCityRequest request =
                new CreateCityRequest(
                        " Kannur ",
                        " Kerala ",
                        " in ",
                        11.8745,
                        75.3704
                );

        when(cityRepository
                .existsByNameIgnoreCaseAndCountryIgnoreCase(
                        "Kannur",
                        "in"
                ))
                .thenReturn(false);

        City savedCity = City.builder()
                .id(1L)
                .name("KANNUR")
                .state("KERALA")
                .country("IN")
                .latitude(11.8745)
                .longitude(75.3704)
                .active(true)
                .build();

        when(cityRepository.save(any(City.class)))
                .thenReturn(savedCity);

        when(userService.getUserId(any()))
                .thenReturn(null);

        CityResponse response =
                cityService.createCity(request);

        assertEquals(1L, response.id());
        assertEquals("KANNUR", response.name());
        assertEquals("KERALA", response.state());
        assertEquals("IN", response.country());
        assertTrue(response.active());

        verify(cityRepository).save(argThat(city ->
                city.getName().equals("KANNUR")
                        && city.getState().equals("KERALA")
                        && city.getCountry().equals("IN")
                        && city.getLatitude().equals(11.8745)
                        && city.getLongitude().equals(75.3704)
        ));

        verify(auditService).log(
                isNull(),
                eq(AuditAction.CITY_ADDED),
                eq(AuditEntityType.CITY),
                eq(1L),
                eq("City added: KANNUR")
        );
    }

    @Test
    void createCity_shouldRejectDuplicateCity() {

        CreateCityRequest request =
                new CreateCityRequest(
                        "Kannur",
                        "Kerala",
                        "IN",
                        11.8745,
                        75.3704
                );

        when(cityRepository
                .existsByNameIgnoreCaseAndCountryIgnoreCase(
                        "Kannur",
                        "IN"
                ))
                .thenReturn(true);

        assertThrows(
                CityAlreadyExistsException.class,
                () -> cityService.createCity(request)
        );

        verify(cityRepository, never())
                .save(any());
    }

    // ---------------------------------------------------------
    // GET CITIES
    // ---------------------------------------------------------

    @Test
    void getAllCities_shouldReturnCities() {

        City city = City.builder()
                .id(1L)
                .name("KANNUR")
                .state("KERALA")
                .country("IN")
                .latitude(11.8745)
                .longitude(75.3704)
                .active(true)
                .build();

        when(cityRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(city));

        List<CityResponse> result =
                cityService.getAllCities();

        assertEquals(1, result.size());
        assertEquals("KANNUR", result.get(0).name());
    }

    @Test
    void getActiveCities_shouldReturnOnlyActiveCities() {

        City city = City.builder()
                .id(1L)
                .name("KANNUR")
                .state("KERALA")
                .country("IN")
                .active(true)
                .build();

        when(cityRepository.findAllByActiveTrueOrderByNameAsc())
                .thenReturn(List.of(city));

        var result =
                cityService.getActiveCities();

        assertEquals(1, result.size());
        assertEquals("KANNUR", result.get(0).name());
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    @Test
    void deleteCity_shouldDeleteExistingCity() {

        City city = City.builder()
                .id(1L)
                .name("KANNUR")
                .build();

        when(cityRepository.findById(1L))
                .thenReturn(Optional.of(city));

        when(userService.getUserId(any()))
                .thenReturn(null);

        cityService.deleteCity(1L);

        verify(cityRepository).delete(city);

        verify(auditService).log(
                isNull(),
                eq(AuditAction.CITY_DELETED),
                eq(AuditEntityType.CITY),
                isNull(),
                eq("City Deleted: KANNUR")
        );
    }

    @Test
    void deleteCity_shouldThrowWhenCityDoesNotExist() {

        when(cityRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                CityNotFoundException.class,
                () -> cityService.deleteCity(99L)
        );

        verify(cityRepository, never())
                .delete(any());
    }

    // ---------------------------------------------------------
    // DEACTIVATE
    // ---------------------------------------------------------

    @Test
    void deactivateCity_shouldSetActiveFalse() {

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

        when(cityRepository.save(any(City.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userService.getUserId(any()))
                .thenReturn(null);

        CityResponse response =
                cityService.deactivateCity(1L);

        assertFalse(response.active());

        verify(cityRepository).save(argThat(
                c -> !c.getActive()
        ));

        verify(auditService).log(
                isNull(),
                eq(AuditAction.CITY_DEACTIVATED),
                eq(AuditEntityType.CITY),
                eq(1L),
                eq("City Deactivated: KANNUR")
        );
    }

    @Test
    void deactivateCity_shouldThrowWhenCityDoesNotExist() {

        when(cityRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                CityNotFoundException.class,
                () -> cityService.deactivateCity(99L)
        );
    }
}