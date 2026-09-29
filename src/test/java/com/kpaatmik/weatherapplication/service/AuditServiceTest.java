package com.kpaatmik.weatherapplication.service;

import com.kpaatmik.weatherapplication.audit.AuditAction;
import com.kpaatmik.weatherapplication.audit.AuditEntityType;
import com.kpaatmik.weatherapplication.entity.AuditLog;
import com.kpaatmik.weatherapplication.entity.User;
import com.kpaatmik.weatherapplication.repository.AuditLogRepository;
import com.kpaatmik.weatherapplication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditServiceTest {

	private AuditLogRepository auditLogRepository;
	private UserRepository userRepository;
	private AuditService auditService;

	@BeforeEach
	void setUp() {

		auditLogRepository = mock(AuditLogRepository.class);
		userRepository = mock(UserRepository.class);

		auditService = new AuditService(auditLogRepository, userRepository);
	}

	// =========================================================
	// USER ID PROVIDED + USER EXISTS
	// =========================================================

	@Test
	void log_shouldSaveAuditLogWithUser_whenUserExists() {

		// Arrange
		Long userId = 1L;

		User user = new User();
		user.setId(userId);
		user.setUsername("aatmik");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// Act
		auditService.log(userId, AuditAction.WEATHER_SEARCHED, AuditEntityType.WEATHER, 10L, "Weather searched: 10");

		// Assert
		verify(userRepository).findById(userId);

		verify(auditLogRepository).save(any(AuditLog.class));

		// Capture the actual AuditLog that was saved
		var captor = org.mockito.ArgumentCaptor.forClass(AuditLog.class);

		verify(auditLogRepository).save(captor.capture());

		AuditLog savedAuditLog = captor.getValue();

		assertNotNull(savedAuditLog);

		assertSame(user, savedAuditLog.getUser());

		assertEquals(AuditAction.WEATHER_SEARCHED.name(), savedAuditLog.getAction());

		assertEquals(AuditEntityType.WEATHER.name(), savedAuditLog.getEntityType());

		assertEquals(10L, savedAuditLog.getEntityId());

		assertEquals("Weather searched: 10", savedAuditLog.getDetails());

		assertNotNull(savedAuditLog.getTimestamp());
	}

	// =========================================================
	// USER ID PROVIDED + USER DOES NOT EXIST
	// =========================================================

	@Test
	void log_shouldSaveAuditLogWithNullUser_whenUserDoesNotExist() {

		// Arrange
		Long userId = 99L;

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// Act
		auditService.log(userId, AuditAction.WEATHER_SEARCHED, AuditEntityType.WEATHER, 20L, "Weather searched: 20");

		// Assert
		verify(userRepository).findById(userId);

		var captor = org.mockito.ArgumentCaptor.forClass(AuditLog.class);

		verify(auditLogRepository).save(captor.capture());

		AuditLog savedAuditLog = captor.getValue();

		assertNotNull(savedAuditLog);

		assertNull(savedAuditLog.getUser());

		assertEquals(AuditAction.WEATHER_SEARCHED.name(), savedAuditLog.getAction());

		assertEquals(AuditEntityType.WEATHER.name(), savedAuditLog.getEntityType());

		assertEquals(20L, savedAuditLog.getEntityId());

		assertEquals("Weather searched: 20", savedAuditLog.getDetails());

		assertNotNull(savedAuditLog.getTimestamp());
	}

	// =========================================================
	// USER ID IS NULL
	// =========================================================

	@Test
	void log_shouldNotLookupUser_whenUserIdIsNull() {

		// Act
		auditService.log(null, AuditAction.USER_REGISTERED, AuditEntityType.USER, null, "User registered");

		// Assert
		verifyNoInteractions(userRepository);

		var captor = org.mockito.ArgumentCaptor.forClass(AuditLog.class);

		verify(auditLogRepository).save(captor.capture());

		AuditLog savedAuditLog = captor.getValue();

		assertNotNull(savedAuditLog);

		assertNull(savedAuditLog.getUser());

		assertEquals(AuditAction.USER_REGISTERED.name(), savedAuditLog.getAction());

		assertEquals(AuditEntityType.USER.name(), savedAuditLog.getEntityType());

		assertNull(savedAuditLog.getEntityId());

		assertEquals("User registered", savedAuditLog.getDetails());

		assertNotNull(savedAuditLog.getTimestamp());
	}
}