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

@Service
@AllArgsConstructor
public class CityService {

	private final OpenWeatherGeocodingClient geocodingClient;
	private final CityRepository cityRepository;
	private final AuditService auditService;

	private final UserService userService;

	public List<CitySuggestionResponse> searchCities(String query) {

		OpenWeatherGeocodingResponse[] results = geocodingClient.searchCity(query);

		return Arrays.stream(results).map(result -> new CitySuggestionResponse(result.name(), result.state(),
				result.country(), result.lat(), result.lon())

		).toList();
	}

	@Transactional
	public CityResponse createCity(CreateCityRequest request) {

		boolean exists = cityRepository.existsByNameIgnoreCaseAndCountryIgnoreCase(request.name().strip(),
				request.country().strip());

		if (exists) {
			throw new CityAlreadyExistsException(request.name(), request.country());
		}

		City city = City.builder().name(request.name().strip().toUpperCase())
				.state(request.state() != null ? request.state().strip().toUpperCase() : null)
				.country(request.country().strip().toUpperCase()).latitude(request.latitude())
				.longitude(request.longitude()).build();

		try {

			City savedCity = cityRepository.save(city);

			auditService.log(userService.getUserId(SecurityUtil.getCurrentUsername()), AuditAction.CITY_ADDED,
					AuditEntityType.CITY, savedCity.getId(), "City added: " + savedCity.getName()

			);

			return covertToCityResponse(savedCity);

		} catch (DataIntegrityViolationException e) {

			throw new CityAlreadyExistsException(request.name(), request.country());
		}
	}

	@Transactional(readOnly = true)
	public List<CityResponse> getAllCities() {

		return cityRepository.findAllByOrderByNameAsc().stream().map(city -> covertToCityResponse(city)).toList();
	}

	@Transactional(readOnly = true)
	public List<PublicCityResponse> getActiveCities() {

		return cityRepository.findAllByActiveTrueOrderByNameAsc().stream()
				.map(city -> new PublicCityResponse(city.getId(), city.getName(), city.getState(), city.getCountry()))
				.toList();
	}

	@Transactional
	public void deleteCity(Long id) {

		City city = cityRepository.findById(id).orElseThrow(() -> new CityNotFoundException(id));
		String cityName = city.getName();

		cityRepository.delete(city);
		auditService.log(userService.getUserId(SecurityUtil.getCurrentUsername()), AuditAction.CITY_DELETED,
				AuditEntityType.CITY, null, "City Deleted: " + cityName

		);
	}

	@Transactional
	@CacheEvict(value = "weather", key = "#cityId")
	public CityResponse deactivateCity(Long cityId) {

		City city = cityRepository.findById(cityId).orElseThrow(() -> new CityNotFoundException(cityId));

		city.setActive(false);

		City updatedCity = cityRepository.save(city);
		auditService.log(userService.getUserId(SecurityUtil.getCurrentUsername()), AuditAction.CITY_DEACTIVATED,
				AuditEntityType.CITY, updatedCity.getId(), "City Deactivated: " + updatedCity.getName()

		);

		return covertToCityResponse(updatedCity);
	}

	private CityResponse covertToCityResponse(City savedCity) {

		return new CityResponse(savedCity.getId(), savedCity.getName(), savedCity.getState(), savedCity.getCountry(),
				savedCity.getLatitude(), savedCity.getLongitude(), savedCity.getActive(), savedCity.getCreatedAt(),
				savedCity.getUpdatedAt());
	}

}
