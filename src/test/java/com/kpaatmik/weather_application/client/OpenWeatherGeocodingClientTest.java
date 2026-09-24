package com.kpaatmik.weather_application.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.kpaatmik.weather_application.config.OpenWeatherProperties;
import com.kpaatmik.weather_application.dto.response.OpenWeatherGeocodingResponse;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OpenWeatherGeocodingClientTest {

    private OpenWeatherProperties properties;
    private MockRestServiceServer server;
    private OpenWeatherGeocodingClient client;

    @BeforeEach
    void setUp() {
        properties = new OpenWeatherProperties();
        properties.setApiKey("test-key");

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://geocode.test");

        server = MockRestServiceServer.bindTo(builder).build();
        client = new OpenWeatherGeocodingClient(builder.build(), properties);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void searchCity_shouldCallGeocodingEndpointAndMapResults() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString(
                "/direct?q=Kannur&limit=5&appid=test-key")))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("""
                    [
                      {
                        "name":"Kannur",
                        "lat":11.8745,
                        "lon":75.3704,
                        "country":"IN",
                        "state":"Kerala"
                      }
                    ]
                    """, org.springframework.http.MediaType.APPLICATION_JSON));

        OpenWeatherGeocodingResponse[] results = client.searchCity("Kannur");

        assertEquals(1, results.length);
        assertEquals("Kannur", results[0].name());
        assertEquals(75.3704, results[0].lon());
    }

    @Test
    void searchCity_shouldReturnEmptyArrayForEmptyProviderResponse() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/direct")))
                .andRespond(withSuccess("[]", org.springframework.http.MediaType.APPLICATION_JSON));

        assertEquals(0, client.searchCity("unknown").length);
    }
}
