package com.kpaatmik.weather_application.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kpaatmik.weather_application.audit.AuditAction;
import com.kpaatmik.weather_application.audit.AuditEntityType;
import com.kpaatmik.weather_application.client.OpenWeatherGeocodingClient;
import com.kpaatmik.weather_application.dto.request.CreateCityRequest;
import com.kpaatmik.weather_application.dto.response.CityResponse;
import com.kpaatmik.weather_application.dto.response.CitySuggestionResponse;
import com.kpaatmik.weather_application.dto.response.OpenWeatherGeocodingResponse;
import com.kpaatmik.weather_application.dto.response.PublicCityResponse;
import com.kpaatmik.weather_application.entity.City;
import com.kpaatmik.weather_application.exception.CityAlreadyExistsException;
import com.kpaatmik.weather_application.exception.CityNotFoundException;
import com.kpaatmik.weather_application.repository.CityRepository;
import com.kpaatmik.weather_application.security.SecurityUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class CityService {

    private final OpenWeatherGeocodingClient geocodingClient;
    private final CityRepository cityRepository;
    private final AuditService auditService;
    private final UserService userService;


    public List<CitySuggestionResponse> searchCities(String query) {

        log.info("City search request received: query={}", query);

        OpenWeatherGeocodingResponse[] results =
                geocodingClient.searchCity(query);

        log.debug("Geocoding API returned {} results for query={}",
                results.length, query);

        List<CitySuggestionResponse> suggestions =
                Arrays.stream(results)
                        .map(result -> new CitySuggestionResponse(
                                result.name(),
                                result.state(),
                                result.country(),
                                result.lat(),
                                result.lon()
                        ))
                        .toList();

        log.info("City search completed: query={}, results={}",
                query, suggestions.size());

        return suggestions;
    }


    @Transactional
    public CityResponse createCity(CreateCityRequest request) {

        log.info("Creating city: name={}, country={}",
                request.name(), request.country());

        boolean exists =
                cityRepository.existsByNameIgnoreCaseAndCountryIgnoreCase(
                        request.name().strip(),
                        request.country().strip()
                );

        if (exists) {

            log.warn(
                    "City creation rejected: city already exists, name={}, country={}",
                    request.name(),
                    request.country()
            );

            throw new CityAlreadyExistsException(
                    request.name(),
                    request.country()
            );
        }

        City city = City.builder()
                .name(request.name().strip().toUpperCase())
                .state(request.state() != null
                        ? request.state().strip().toUpperCase()
                        : null)
                .country(request.country().strip().toUpperCase())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        try {

            City savedCity = cityRepository.save(city);

            log.info(
                    "City created successfully: cityId={}, name={}, country={}",
                    savedCity.getId(),
                    savedCity.getName(),
                    savedCity.getCountry()
            );

            auditService.log(
                    userService.getUserId(
                            SecurityUtil.getCurrentUsername()
                    ),
                    AuditAction.CITY_ADDED,
                    AuditEntityType.CITY,
                    savedCity.getId(),
                    "City added: " + savedCity.getName()
            );

            return covertToCityResponse(savedCity);

        } catch (DataIntegrityViolationException e) {

            log.warn(
                    "City creation failed due to data integrity violation: name={}, country={}",
                    request.name(),
                    request.country()
            );

            throw new CityAlreadyExistsException(
                    request.name(),
                    request.country()
            );
        }
    }


    @Transactional(readOnly = true)
    public List<CityResponse> getAllCities() {

        log.debug("Fetching all cities");

        List<CityResponse> cities =
                cityRepository.findAllByOrderByNameAsc()
                        .stream()
                        .map(this::covertToCityResponse)
                        .toList();

        log.info("Retrieved all cities: count={}", cities.size());

        return cities;
    }


    @Transactional(readOnly = true)
    public List<PublicCityResponse> getActiveCities() {

        log.debug("Fetching active cities");

        List<PublicCityResponse> cities =
                cityRepository.findAllByActiveTrueOrderByNameAsc()
                        .stream()
                        .map(city -> new PublicCityResponse(
                                city.getId(),
                                city.getName(),
                                city.getState(),
                                city.getCountry()
                        ))
                        .toList();

        log.info("Retrieved active cities: count={}", cities.size());

        return cities;
    }


    @Transactional
    public void deleteCity(Long id) {

        log.info("Deleting city: cityId={}", id);

        City city = cityRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn("City deletion failed: city not found, cityId={}",
                            id);

                    return new CityNotFoundException(id);
                });

        String cityName = city.getName();

        cityRepository.delete(city);

        auditService.log(
                userService.getUserId(
                        SecurityUtil.getCurrentUsername()
                ),
                AuditAction.CITY_DELETED,
                AuditEntityType.CITY,
                null,
                "City Deleted: " + cityName
        );

        log.info(
                "City deleted successfully: cityId={}, name={}",
                id,
                cityName
        );
    }


    @Transactional
    @CacheEvict(value = "weather", key = "#cityId")
    public CityResponse deactivateCity(Long cityId) {

        log.info("Deactivating city: cityId={}", cityId);

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> {

                    log.warn(
                            "City deactivation failed: city not found, cityId={}",
                            cityId
                    );

                    return new CityNotFoundException(cityId);
                });

        city.setActive(false);

        City updatedCity = cityRepository.save(city);

        auditService.log(
                userService.getUserId(
                        SecurityUtil.getCurrentUsername()
                ),
                AuditAction.CITY_DEACTIVATED,
                AuditEntityType.CITY,
                updatedCity.getId(),
                "City Deactivated: " + updatedCity.getName()
        );

        log.info(
                "City deactivated successfully: cityId={}, name={}",
                updatedCity.getId(),
                updatedCity.getName()
        );

        log.debug(
                "Weather cache evicted for cityId={}",
                cityId
        );

        return covertToCityResponse(updatedCity);
    }


    private CityResponse covertToCityResponse(City savedCity) {

        return new CityResponse(
                savedCity.getId(),
                savedCity.getName(),
                savedCity.getState(),
                savedCity.getCountry(),
                savedCity.getLatitude(),
                savedCity.getLongitude(),
                savedCity.getActive(),
                savedCity.getCreatedAt(),
                savedCity.getUpdatedAt()
        );
    }
}