package com.kpaatmik.weather_application.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kpaatmik.weather_application.client.OpenWeatherClient;
import com.kpaatmik.weather_application.client.OpenWeatherGeocodingClient;
import com.kpaatmik.weather_application.dto.CityResponse;
import com.kpaatmik.weather_application.dto.CitySuggestionResponse;
import com.kpaatmik.weather_application.dto.CreateCityRequest;
import com.kpaatmik.weather_application.dto.OpenWeatherGeocodingResponse;
import com.kpaatmik.weather_application.dto.PublicCityResponse;
import com.kpaatmik.weather_application.entity.City;
import com.kpaatmik.weather_application.exception.CityAlreadyExistsException;
import com.kpaatmik.weather_application.exception.CityNotFoundException;
import com.kpaatmik.weather_application.repository.CityRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CityService {
	private final OpenWeatherClient weatherClient;
	private final OpenWeatherGeocodingClient geocodingClient;
	private final CityRepository cityRepository;

	public List<CitySuggestionResponse> searchCities(String query) {

		OpenWeatherGeocodingResponse[] results = geocodingClient.searchCity(query);

		return Arrays.stream(results).map(result -> new CitySuggestionResponse(result.name(), result.state(),
				result.country(), result.lat(), result.lat())

		).toList();
	}

	@Transactional
	public CityResponse createCity(CreateCityRequest request) {

//	    boolean exists = cityRepository.existsByNameIgnoreCaseAndCountryIgnoreCase(
//	            request.name().strip(),
//	            request.country().strip()
//	    );
	    
	    
	    
	    String name = request.name().strip();
	    String country = request.country().strip();

	    boolean exists = cityRepository
	            .existsByNameIgnoreCaseAndCountryIgnoreCase(name, country);

	    System.out.println("Checking city: " + name);
	    System.out.println("Checking country: " + country);
	    System.out.println("City exists: " + exists);

	    if (exists) {
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

	        return covertToCityResponse(savedCity);

	    } catch (DataIntegrityViolationException e) {

	        throw new CityAlreadyExistsException(
	                request.name(),
	                request.country()
	        );
	    }
	}
	
	
	@Transactional(readOnly = true)
    public List<CityResponse> getAllCities() {

        return cityRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(city->covertToCityResponse(city))
                .toList();
    }
	
	@Transactional(readOnly = true)
	public List<PublicCityResponse> getActiveCities() {

	    return cityRepository
	            .findAllByActiveTrueOrderByNameAsc()
	            .stream()
	            .map(city -> new PublicCityResponse(
	                    city.getId(),
	                    city.getName(),
	                    city.getState(),
	                    city.getCountry()
	            ))
	            .toList();
	}
	
	
	
	@Transactional
    public void deleteCity(Long id) {

        City city = cityRepository.findById(id)
                .orElseThrow(() ->
                        new CityNotFoundException(id)
                );

        cityRepository.delete(city);
    }
	
	
	
	
	@Transactional
	@CacheEvict(value = "weather", key = "#cityId")
	public CityResponse deactivateCity(Long cityId) {

	    City city = cityRepository.findById(cityId)
	            .orElseThrow(() ->
	                    new CityNotFoundException(cityId)
	            );

	    city.setActive(false);

	    City updatedCity = cityRepository.save(city);

	    return covertToCityResponse(updatedCity);
	}
	
	
	
	private CityResponse covertToCityResponse(City savedCity) {
		
		return new CityResponse(savedCity.getId(),
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
