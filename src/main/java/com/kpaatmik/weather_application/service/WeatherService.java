package com.kpaatmik.weather_application.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kpaatmik.weather_application.audit.AuditAction;
import com.kpaatmik.weather_application.audit.AuditEntityType;
import com.kpaatmik.weather_application.client.OpenWeatherClient;
import com.kpaatmik.weather_application.dto.response.OpenWeatherResponse;
import com.kpaatmik.weather_application.dto.response.WeatherResponse;
import com.kpaatmik.weather_application.entity.City;
import com.kpaatmik.weather_application.exception.CityNotFoundException;
import com.kpaatmik.weather_application.repository.CityRepository;
import com.kpaatmik.weather_application.security.SecurityUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class WeatherService {

    private final CityRepository cityRepository;
    private final OpenWeatherClient weatherClient;
    private final AuditService auditService;
    private final UserService userService;

    @Cacheable(value = "weather", key = "#cityId")
    @Transactional
    public WeatherResponse getWeather(Long cityId) {

        log.info("Weather request received: cityId={}", cityId);

        auditService.log(
                userService.getUserId(
                        SecurityUtil.getCurrentUsername()
                ),
                AuditAction.WEATHER_SEARCHED,
                AuditEntityType.WEATHER,
                null,
                "Weather Searched: " + cityId
        );

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