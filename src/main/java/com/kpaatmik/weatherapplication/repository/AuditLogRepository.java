package com.kpaatmik.weatherapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kpaatmik.weatherapplication.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}