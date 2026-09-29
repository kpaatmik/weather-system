package com.kpaatmik.weatherapplication.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kpaatmik.weatherapplication.audit.AuditAction;
import com.kpaatmik.weatherapplication.audit.AuditEntityType;
import com.kpaatmik.weatherapplication.entity.AuditLog;
import com.kpaatmik.weatherapplication.entity.User;
import com.kpaatmik.weatherapplication.repository.AuditLogRepository;
import com.kpaatmik.weatherapplication.repository.UserRepository;

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