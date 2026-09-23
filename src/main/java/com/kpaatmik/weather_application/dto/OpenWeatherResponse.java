package com.kpaatmik.weather_application.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenWeatherResponse(

        Main main,

        Wind wind,

        List<Weather> weather

) {

    public record Main(

            Double temp,

            @JsonProperty("feels_like")
            Double feelsLike,

            Integer humidity

    ) {
    }

    public record Wind(

            Double speed

    ) {
    }

    public record Weather(

            String main,

            String description,

            String icon

    ) {
    }
}