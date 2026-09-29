package com.kpaatmik.weatherapplication.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void onCreate_shouldSetCreatedAtAndUpdatedAt() {

		User user = new User();

		assertNull(user.getCreatedAt());
		assertNull(user.getUpdatedAt());

		user.onCreate();

		assertNotNull(user.getCreatedAt());
		assertNotNull(user.getUpdatedAt());

		assertEquals(user.getCreatedAt(), user.getUpdatedAt());
	}

	@Test
	void onUpdate_shouldUpdateUpdatedAt() {

		User user = new User();

		LocalDateTime createdAt = LocalDateTime.now().minusHours(1);

		LocalDateTime oldUpdatedAt = LocalDateTime.now().minusMinutes(10);

		user.setCreatedAt(createdAt);
		user.setUpdatedAt(oldUpdatedAt);

		user.onUpdate();

		assertEquals(createdAt, user.getCreatedAt());

		assertNotNull(user.getUpdatedAt());

		assertTrue(user.getUpdatedAt().isAfter(oldUpdatedAt));
	}
}