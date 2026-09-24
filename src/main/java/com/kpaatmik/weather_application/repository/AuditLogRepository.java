package com.kpaatmik.weather_application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kpaatmik.weather_application.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}