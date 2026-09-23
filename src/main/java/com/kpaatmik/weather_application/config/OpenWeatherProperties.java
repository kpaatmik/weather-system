package com.kpaatmik.weather_application.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openweather")
public class OpenWeatherProperties {

    private String apiKey;
    private String weatherBaseUrl;
    private String geocodingBaseUrl;
    private Duration connectTimeout;
    private Duration readTimeout;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getWeatherBaseUrl() {
    	System.out.println(weatherBaseUrl);
        return weatherBaseUrl;
    }

    public void setWeatherBaseUrl(String weatherBaseUrl) {
        this.weatherBaseUrl = weatherBaseUrl;
    }

    public String getGeocodingBaseUrl() {
        return geocodingBaseUrl;
    }

    public void setGeocodingBaseUrl(String geocodingBaseUrl) {
        this.geocodingBaseUrl = geocodingBaseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }
}