package com.kpaatmik.weather_application.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.kpaatmik.weather_application.config.OpenWeatherProperties;
import com.kpaatmik.weather_application.dto.response.OpenWeatherResponse;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OpenWeatherClientTest {

    private OpenWeatherProperties properties;
    private MockRestServiceServer server;
    private OpenWeatherClient client;

    @BeforeEach
    void setUp() {
        properties = new OpenWeatherProperties();
        properties.setApiKey("test-key");

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://openweather.test");

        server = MockRestServiceServer.bindTo(builder).build();
        client = new OpenWeatherClient(builder.build(), properties);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void getWeather_shouldCallCorrectEndpointAndMapResponse() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString(
                "/weather?lat=11.8745&lon=75.3704&appid=test-key&units=metric")))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("""
                    {
                      "main":{"temp":30.5,"feels_like":31.0,"humidity":78},
                      "wind":{"speed":4.2},
                      "weather":[{"main":"Clouds","description":"broken clouds","icon":"04d"}]
                    }
                    """, org.springframework.http.MediaType.APPLICATION_JSON));

        OpenWeatherResponse response = client.getWeather(11.8745, 75.3704);

        assertEquals(30.5, response.main().temp());
        assertEquals(31.0, response.main().feelsLike());
        assertEquals(78, response.main().humidity());
        assertEquals(4.2, response.wind().speed());
        assertEquals("Clouds", response.weather().get(0).main());
    }

    @Test
    void getWeather_shouldPropagateProviderHttpError() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/weather")))
                .andRespond(withBadRequest());

        assertThrows(Exception.class,
                () -> client.getWeather(11.8745, 75.3704));
    }
}
