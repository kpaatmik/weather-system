package com.kpaatmik.weather_application.service;

import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kpaatmik.weather_application.client.OpenWeatherClient;
import com.kpaatmik.weather_application.dto.OpenWeatherResponse;
import com.kpaatmik.weather_application.dto.WeatherResponse;
import com.kpaatmik.weather_application.entity.City;
import com.kpaatmik.weather_application.exception.CityNotFoundException;
import com.kpaatmik.weather_application.repository.CityRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class WeatherService {

    private final CityRepository cityRepository;
    private final OpenWeatherClient weatherClient;

    @Cacheable(
            value = "weather",
            key = "#cityId"
    )
    @Transactional(readOnly = true)
    public WeatherResponse getWeather(Long cityId) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() ->
                        new CityNotFoundException(cityId)
                );

        if (!Boolean.TRUE.equals(city.getActive())) {
            throw new CityNotFoundException(cityId);
        }

        OpenWeatherResponse weather =
                weatherClient.getWeather(
                        city.getLatitude(),
                        city.getLongitude()
                );

        OpenWeatherResponse.Main main = weather.main();

        OpenWeatherResponse.Wind wind = weather.wind();

        OpenWeatherResponse.Weather currentWeather =
                weather.weather().get(0);

        return new WeatherResponse(
                city.getId(),
                city.getName(),
                main.temp(),
                main.feelsLike(),
                main.humidity(),
                wind.speed(),
                currentWeather.main(),
                currentWeather.description()
        );
    }
}