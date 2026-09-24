package com.kpaatmik.weather_application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kpaatmik.weather_application.audit.AuditAction;
import com.kpaatmik.weather_application.audit.AuditEntityType;
import com.kpaatmik.weather_application.entity.AuditLog;
import com.kpaatmik.weather_application.entity.User;
import com.kpaatmik.weather_application.repository.AuditLogRepository;
import com.kpaatmik.weather_application.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

	private final AuditLogRepository auditLogRepository;
	private final UserRepository userRepository;

	@Transactional
	public void log(Long userId, AuditAction action, AuditEntityType entityType, Long entityId, String details) {

		User user = null;

		if (userId != null) {
			user = userRepository.findById(userId).orElse(null);
		}

		AuditLog auditLog = AuditLog.builder().user(user).action(action.name()).entityType(entityType.name())
				.entityId(entityId).details(details).timestamp(LocalDateTime.now()).build();

		auditLogRepository.save(auditLog);
	}

}