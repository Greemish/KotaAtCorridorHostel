package com.kota.repository;

import com.kota.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByPerformedByIdOrderByCreatedAtDesc(Long performedById);

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
