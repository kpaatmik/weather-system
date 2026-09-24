package com.kpaatmik.weather_application.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.kpaatmik.weather_application.config.OpenWeatherProperties;
import com.kpaatmik.weather_application.dto.response.OpenWeatherGeocodingResponse;

@Component
public class OpenWeatherGeocodingClient {

	private final RestClient restClient;
	private final OpenWeatherProperties properties;

	public OpenWeatherGeocodingClient(@Qualifier("geocodingRestClient") RestClient restClient,
			OpenWeatherProperties properties) {

		this.restClient = restClient;
		this.properties = properties;
	}

	public OpenWeatherGeocodingResponse[] searchCity(String cityName) {

		return restClient.get()
				.uri(uriBuilder -> uriBuilder.path("/direct").queryParam("q", cityName).queryParam("limit", 5)
						.queryParam("appid", properties.getApiKey()).build())
				.retrieve().body(OpenWeatherGeocodingResponse[].class);
	}
}
