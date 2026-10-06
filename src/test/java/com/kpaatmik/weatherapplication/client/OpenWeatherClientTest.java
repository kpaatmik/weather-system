package com.kpaatmik.weatherapplication.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import com.kpaatmik.weatherapplication.config.OpenWeatherProperties;
import com.kpaatmik.weatherapplication.dto.response.OpenWeatherResponse;
import com.kpaatmik.weatherapplication.exception.WeatherServiceException;

class OpenWeatherClientTest {

	private RestClient restClient;
	private OpenWeatherProperties properties;
	private OpenWeatherClient openWeatherClient;

	@SuppressWarnings("rawtypes")
	private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

	@SuppressWarnings("rawtypes")
	private RestClient.RequestHeadersSpec requestHeadersSpec;

	private RestClient.ResponseSpec responseSpec;

	@BeforeEach
	void setUp() {

		restClient = mock(RestClient.class);
		properties = mock(OpenWeatherProperties.class);

		requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
		requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
		responseSpec = mock(RestClient.ResponseSpec.class);

		openWeatherClient = new OpenWeatherClient(restClient, properties);
	}

	@Test
	void getWeather_shouldReturnWeatherResponse() {

		OpenWeatherResponse expectedResponse = mock(OpenWeatherResponse.class);

		when(properties.getApiKey()).thenReturn("test-api-key");

		doReturn(requestHeadersUriSpec).when(restClient).get();

		doAnswer(invocation -> {

			Function<UriBuilder, URI> uriFunction = invocation.getArgument(0);

			URI uri = uriFunction.apply(new DefaultUriBuilderFactory().builder());

			assertEquals("/weather?lat=11.8745&lon=75.3704&appid=test-api-key&units=metric", uri.toString());

			return requestHeadersSpec;

		}).when(requestHeadersUriSpec).uri(any(Function.class));

		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

		when(responseSpec.body(OpenWeatherResponse.class)).thenReturn(expectedResponse);

		OpenWeatherResponse result = openWeatherClient.getWeather(11.8745, 75.3704);

		assertEquals(expectedResponse, result);
	}

	@Test
	void getWeather_shouldThrowWeatherServiceException_whenConnectionFails() {

		doThrow(new ResourceAccessException("Connection failed")).when(restClient).get();

		WeatherServiceException exception = assertThrows(WeatherServiceException.class,
				() -> openWeatherClient.getWeather(11.8745, 75.3704));

		assertEquals("Unable to connect to OpenWeather API", exception.getMessage());
	}

	@Test
	void getWeather_shouldThrowWeatherServiceException_whenApiFails() {

		doThrow(new RestClientException("API error")).when(restClient).get();

		WeatherServiceException exception = assertThrows(WeatherServiceException.class,
				() -> openWeatherClient.getWeather(11.8745, 75.3704));

		assertEquals("OpenWeather API returned an error", exception.getMessage());
	}
}