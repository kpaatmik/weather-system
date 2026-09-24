package com.kpaatmik.weather_application.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.kpaatmik.weather_application.config.OpenWeatherProperties;
import com.kpaatmik.weather_application.dto.response.OpenWeatherResponse;

@Component
public class OpenWeatherClient {

	private final RestClient restClient;
	private final OpenWeatherProperties properties;

	public OpenWeatherClient(@Qualifier("weatherRestClient") RestClient restClient, OpenWeatherProperties properties) {

		this.restClient = restClient;
		this.properties = properties;
	}

	public OpenWeatherResponse getWeather(Double latitude, Double longitude) {

		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/weather").queryParam("lat", latitude).queryParam("lon", longitude)
						.queryParam("appid", properties.getApiKey()).queryParam("units", "metric").build())
				.retrieve().body(OpenWeatherResponse.class);
	}
}
