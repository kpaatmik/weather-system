package com.kpaatmik.weather_application.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenWeatherProperties.class)
public class OpenWeatherConfig {


    @Bean("weatherRestClient")
    public RestClient weatherRestClient(OpenWeatherProperties properties) {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(properties.getWeatherBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean("geocodingRestClient")

    public RestClient geocodingRestClient(OpenWeatherProperties properties) {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(properties.getGeocodingBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}