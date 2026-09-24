package com.kpaatmik.weather_application.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class EntityLifecycleTest {

    @Test
    void userOnCreate_shouldSetCreatedAndUpdatedTimestamps() {
        User user = User.builder().username("u").email("u@x.com")
                .password("hash").role(Role.USER).active(true).build();

        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertFalse(user.getUpdatedAt().isBefore(user.getCreatedAt()));
    }

    @Test
    void userOnUpdate_shouldRefreshUpdatedTimestamp() throws InterruptedException {
        User user = User.builder().username("u").email("u@x.com")
                .password("hash").role(Role.USER).active(true).build();
        user.onCreate();
        LocalDateTime before = user.getUpdatedAt();

        Thread.sleep(2);
        user.onUpdate();

        assertTrue(user.getUpdatedAt().isAfter(before));
    }

    @Test
    void cityOnCreate_shouldSetTimestamps() {
        City city = City.builder().name("KANNUR").country("IN")
                .latitude(1.0).longitude(2.0).build();

        city.onCreate();

        assertNotNull(city.getCreatedAt());
        assertNotNull(city.getUpdatedAt());
        assertTrue(city.getActive());
    }

    @Test
    void cityOnUpdate_shouldRefreshUpdatedTimestamp() throws InterruptedException {
        City city = City.builder().name("KANNUR").country("IN")
                .latitude(1.0).longitude(2.0).build();
        city.onCreate();
        LocalDateTime before = city.getUpdatedAt();

        Thread.sleep(2);
        city.onUpdate();

        assertTrue(city.getUpdatedAt().isAfter(before));
    }
}
