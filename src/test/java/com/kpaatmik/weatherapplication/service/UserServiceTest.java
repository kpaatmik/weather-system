package com.kpaatmik.weatherapplication.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.kpaatmik.weatherapplication.entity.User;
import com.kpaatmik.weatherapplication.repository.UserRepository;
import com.kpaatmik.weatherapplication.security.SecurityUtil;

class UserServiceTest {

	private UserRepository userRepository;
	private UserService userService;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		userService = new UserService(userRepository);
	}

	// ---------------------------------------------------------
	// USER EXISTS
	// ---------------------------------------------------------

	@Test
	void getUserId_shouldReturnUserId_whenUserExists() {

		// Arrange
		String username = "aatmik";

		User user = new User();
		user.setId(1L);
		user.setUsername(username);

		when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

		try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {

			securityUtil.when(SecurityUtil::getCurrentUsername).thenReturn(username);

			// Act
			Long result = userService.getUserId(username);

			// Assert
			assertNotNull(result);
			assertEquals(1L, result);

			verify(userRepository).findByUsername(username);

			securityUtil.verify(SecurityUtil::getCurrentUsername);
		}
	}

	// ---------------------------------------------------------
	// USER DOES NOT EXIST
	// ---------------------------------------------------------

	@Test
	void getUserId_shouldReturnNull_whenUserDoesNotExist() {

		// Arrange
		String username = "unknown";

		when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

		try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {

			securityUtil.when(SecurityUtil::getCurrentUsername).thenReturn(username);

			// Act
			Long result = userService.getUserId(username);

			// Assert
			assertNull(result);

			verify(userRepository).findByUsername(username);

			securityUtil.verify(SecurityUtil::getCurrentUsername);
		}
	}
}