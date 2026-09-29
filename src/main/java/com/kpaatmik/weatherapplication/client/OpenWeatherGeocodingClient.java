package com.kpaatmik.weatherapplication.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.kpaatmik.weatherapplication.config.OpenWeatherProperties;
import com.kpaatmik.weatherapplication.dto.response.OpenWeatherGeocodingResponse;
import com.kpaatmik.weatherapplication.exception.WeatherServiceException;

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

		try {
			return restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/direct").queryParam("q", cityName).queryParam("limit", 5)
							.queryParam("appid", properties.getApiKey()).build())
					.retrieve().body(OpenWeatherGeocodingResponse[].class);
		} catch (ResourceAccessException ex) {
			throw new WeatherServiceException("Unable to connect to OpenWeather API", ex);
		} catch (RestClientException ex) {
			throw new WeatherServiceException("OpenWeather API returned an error", ex);

		}
	}
}
