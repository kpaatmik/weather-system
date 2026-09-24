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
import org.springframework.dao.DataIntegrityViolationException;

import com.kpaatmik.weather_application.audit.*;
import com.kpaatmik.weather_application.client.OpenWeatherGeocodingClient;
import com.kpaatmik.weather_application.dto.request.CreateCityRequest;
import com.kpaatmik.weather_application.dto.response.*;
import com.kpaatmik.weather_application.entity.City;
import com.kpaatmik.weather_application.exception.*;
import com.kpaatmik.weather_application.repository.CityRepository;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock OpenWeatherGeocodingClient geocodingClient;
    @Mock CityRepository cityRepository;
    @Mock AuditService auditService;
    @Mock UserService userService;

    @InjectMocks CityService cityService;

    private City city;

    @BeforeEach
    void setUp() {
        city = City.builder()
                .id(1L).name("KANNUR").state("KERALA").country("IN")
                .latitude(11.8745).longitude(75.3704).active(true).build();
    }

    @Test
    void searchCities_shouldMapProviderResults() {
        var result = new OpenWeatherGeocodingResponse(
                "Kannur", null, 11.8745, 75.3704, "IN", "Kerala");

        when(geocodingClient.searchCity("Kannur")).thenReturn(
                new OpenWeatherGeocodingResponse[]{result});

        List<CitySuggestionResponse> response = cityService.searchCities("Kannur");

        assertEquals(1, response.size());
        assertEquals("Kannur", response.get(0).name());
        assertEquals("Kerala", response.get(0).state());
        assertEquals("IN", response.get(0).country());
        assertEquals(11.8745, response.get(0).latitude());
        assertEquals(75.3704, response.get(0).longitude());
    }

    @Test
    void searchCities_shouldReturnEmptyListWhenProviderReturnsEmptyArray() {
        when(geocodingClient.searchCity("xyz")).thenReturn(new OpenWeatherGeocodingResponse[0]);

        assertTrue(cityService.searchCities("xyz").isEmpty());
    }

    @Test
    void searchCities_shouldPropagateProviderFailure() {
        when(geocodingClient.searchCity("Kannur"))
                .thenThrow(new RuntimeException("provider down"));

        assertThrows(RuntimeException.class, () -> cityService.searchCities("Kannur"));
    }

    @Test
    void createCity_shouldNormalizeNameStateAndCountryAndAudit() {
        CreateCityRequest request =
                new CreateCityRequest("  Kannur ", "  Kerala ", " in ", 11.8745, 75.3704);

        when(cityRepository.existsByNameIgnoreCaseAndCountryIgnoreCase("Kannur", "in"))
                .thenReturn(false);
                when(cityRepository.save(any(City.class))).thenReturn(city);

        CityResponse response = cityService.createCity(request);

        assertEquals(1L, response.id());
        assertEquals("KANNUR", response.name());

        ArgumentCaptor<City> captor = ArgumentCaptor.forClass(City.class);
        verify(cityRepository).save(captor.capture());

        assertEquals("KANNUR", captor.getValue().getName());
        assertEquals("KERALA", captor.getValue().getState());
        assertEquals("IN", captor.getValue().getCountry());

        verify(auditService).log(isNull(), eq(AuditAction.CITY_ADDED),
                eq(AuditEntityType.CITY), eq(1L), contains("KANNUR"));
    }

    @Test
    void createCity_shouldAllowNullState() {
        CreateCityRequest request =
                new CreateCityRequest("Kannur", null, "IN", 11.8745, 75.3704);

        when(cityRepository.existsByNameIgnoreCaseAndCountryIgnoreCase("Kannur", "IN"))
                .thenReturn(false);
        when(cityRepository.save(any(City.class))).thenReturn(city);

        cityService.createCity(request);

        ArgumentCaptor<City> captor = ArgumentCaptor.forClass(City.class);
        verify(cityRepository).save(captor.capture());
        assertNull(captor.getValue().getState());
    }

    @Test
    void createCity_shouldRejectExistingCity() {
        CreateCityRequest request =
                new CreateCityRequest("Kannur", "Kerala", "IN", 11.8745, 75.3704);

        when(cityRepository.existsByNameIgnoreCaseAndCountryIgnoreCase("Kannur", "IN"))
                .thenReturn(true);

        assertThrows(CityAlreadyExistsException.class,
                () -> cityService.createCity(request));

        verify(cityRepository, never()).save(any());
        verifyNoInteractions(auditService);
    }

    @Test
    void createCity_shouldConvertDatabaseUniqueViolationToBusinessException() {
        CreateCityRequest request =
                new CreateCityRequest("Kannur", "Kerala", "IN", 11.8745, 75.3704);

        when(cityRepository.existsByNameIgnoreCaseAndCountryIgnoreCase("Kannur", "IN"))
                .thenReturn(false);
        when(cityRepository.save(any(City.class)))
                .thenThrow(new DataIntegrityViolationException("unique"));

        assertThrows(CityAlreadyExistsException.class,
                () -> cityService.createCity(request));

        verifyNoInteractions(auditService);
    }

    @Test
    void getAllCities_shouldReturnSortedRepositoryResults() {
        City second = City.builder().id(2L).name("KOZHIKODE").country("IN")
                .latitude(11.25).longitude(75.78).active(true).build();

        when(cityRepository.findAllByOrderByNameAsc()).thenReturn(List.of(city, second));

        List<CityResponse> result = cityService.getAllCities();

        assertEquals(2, result.size());
        assertEquals("KANNUR", result.get(0).name());
        assertEquals("KOZHIKODE", result.get(1).name());
    }

    @Test
    void getActiveCities_shouldMapOnlyRepositoryActiveResults() {
        when(cityRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(city));

        List<PublicCityResponse> result = cityService.getActiveCities();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("KANNUR", result.get(0).name());
    }

    @Test
    void deleteCity_shouldDeleteAndAudit() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        
        cityService.deleteCity(1L);

        verify(cityRepository).delete(city);
        verify(auditService).log(null, AuditAction.CITY_DELETED,
                AuditEntityType.CITY, null, "City Deleted: KANNUR");
    }

    @Test
    void deleteCity_shouldRejectUnknownCity() {
        when(cityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CityNotFoundException.class, () -> cityService.deleteCity(999L));

        verify(cityRepository, never()).delete(any());
        verifyNoInteractions(auditService);
    }

    @Test
    void deactivateCity_shouldMarkInactiveSaveAndAudit() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(cityRepository.save(city)).thenReturn(city);
        
        CityResponse result = cityService.deactivateCity(1L);

        assertFalse(result.active());
        verify(cityRepository).save(city);
        verify(auditService).log(null, AuditAction.CITY_DEACTIVATED,
                AuditEntityType.CITY, 1L, "City Deactivated: KANNUR");
    }

    @Test
    void deactivateCity_shouldRejectUnknownCity() {
        when(cityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CityNotFoundException.class,
                () -> cityService.deactivateCity(999L));

        verify(cityRepository, never()).save(any());
    }
}
