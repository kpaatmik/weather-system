package com.kpaatmik.weather_application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kpaatmik.weather_application.audit.*;
import com.kpaatmik.weather_application.entity.*;
import com.kpaatmik.weather_application.repository.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock AuditLogRepository auditLogRepository;
    @Mock UserRepository userRepository;

    @InjectMocks AuditService auditService;

    @Test
    void log_shouldPersistAuditWithUser() {
        User user = User.builder().id(10L).username("admin").email("a@x.com")
                .password("hash").role(Role.ADMIN).active(true).build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        auditService.log(10L, AuditAction.CITY_ADDED, AuditEntityType.CITY,
                20L, "City added");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog log = captor.getValue();
        assertSame(user, log.getUser());
        assertEquals("CITY_ADDED", log.getAction());
        assertEquals("CITY", log.getEntityType());
        assertEquals(20L, log.getEntityId());
        assertEquals("City added", log.getDetails());
        assertNotNull(log.getTimestamp());
    }

    @Test
    void log_shouldPersistAuditWithoutUserForNullUserId() {
        auditService.log(null, AuditAction.USER_REGISTERED, AuditEntityType.USER,
                1L, "registered");

        verify(userRepository, never()).findById(anyLong());

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertNull(captor.getValue().getUser());
        assertEquals("USER_REGISTERED", captor.getValue().getAction());
        assertNotNull(captor.getValue().getTimestamp());
    }

    @Test
    void log_shouldAllowMissingReferencedUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        auditService.log(999L, AuditAction.USER_LOGIN, AuditEntityType.USER,
                999L, "login");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertNull(captor.getValue().getUser());
    }

    @Test
    void log_shouldPropagateRepositoryFailure() {
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenThrow(new RuntimeException("DB failure"));

        assertThrows(RuntimeException.class,
                () -> auditService.log(null, AuditAction.WEATHER_SEARCHED,
                        AuditEntityType.WEATHER, 1L, "weather"));
    }
}
