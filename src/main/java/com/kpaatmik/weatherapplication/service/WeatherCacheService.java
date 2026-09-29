package com.kpaatmik.weatherapplication.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.kpaatmik.weatherapplication.client.OpenWeatherClient;
import com.kpaatmik.weatherapplication.dto.response.OpenWeatherResponse;
import com.kpaatmik.weatherapplication.dto.response.WeatherResponse;
import com.kpaatmik.weatherapplication.entity.City;
import com.kpaatmik.weatherapplication.exception.CityNotFoundException;
import com.kpaatmik.weatherapplication.repository.CityRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class WeatherCacheService {

	private final CityRepository cityRepository;
	private final OpenWeatherClient weatherClient;

	@Cacheable(value = "weather", key = "#cityId")
	public WeatherResponse getWeather(Long cityId) {

		City city = cityRepository.findById(cityId)
                .orElseThrow(() -> {

                    log.warn(
                            "Weather request failed: city not found, cityId={}",
                            cityId
                    );

                    return new CityNotFoundException(cityId);
                });

        log.debug(
                "City found: cityId={}, name={}, active={}",
                city.getId(),
                city.getName(),
                city.getActive()
        );

        if (!Boolean.TRUE.equals(city.getActive())) {

            log.warn(
                    "Weather request rejected: city is inactive, cityId={}, name={}",
                    city.getId(),
                    city.getName()
            );

            throw new CityNotFoundException(cityId);
        }

        log.debug(
                "Calling OpenWeather API: cityId={}, city={}, latitude={}, longitude={}",
                city.getId(),
                city.getName(),
                city.getLatitude(),
                city.getLongitude()
        );

        OpenWeatherResponse weather =
                weatherClient.getWeather(
                        city.getLatitude(),
                        city.getLongitude()
                );

        log.debug(
                "OpenWeather API response received: cityId={}, city={}",
                city.getId(),
                city.getName()
        );

        OpenWeatherResponse.Main main = weather.main();

        OpenWeatherResponse.Wind wind = weather.wind();

        OpenWeatherResponse.Weather currentWeather =
                weather.weather().get(0);

        WeatherResponse response = new WeatherResponse(
                city.getId(),
                city.getName(),
                main.temp(),
                main.feelsLike(),
                main.humidity(),
                wind.speed(),
                currentWeather.main(),
                currentWeather.description()
        );

        log.info(
                "Weather retrieved successfully: cityId={}, city={}, condition={}",
                city.getId(),
                city.getName(),
                currentWeather.main()
        );

        return response;
	}
}
