package com.kpaatmik.weatherapplication.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class CityTest {

    @Test
    void onCreate_shouldSetCreatedAtAndUpdatedAt() {

        City city = new City();

        assertNull(city.getCreatedAt());
        assertNull(city.getUpdatedAt());

        city.onCreate();

        assertNotNull(city.getCreatedAt());
        assertNotNull(city.getUpdatedAt());

        assertEquals(
                city.getCreatedAt(),
                city.getUpdatedAt()
        );
    }

    @Test
    void onUpdate_shouldUpdateUpdatedAt() {

        City city = new City();

        LocalDateTime createdAt =
                LocalDateTime.now().minusHours(1);

        LocalDateTime oldUpdatedAt =
                LocalDateTime.now().minusMinutes(10);

        city.setCreatedAt(createdAt);
        city.setUpdatedAt(oldUpdatedAt);

        city.onUpdate();

        assertEquals(
                createdAt,
                city.getCreatedAt()
        );

        assertNotNull(city.getUpdatedAt());

        assertTrue(
                city.getUpdatedAt().isAfter(oldUpdatedAt)
        );
    }
}