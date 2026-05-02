package com.kota.service;

import com.kota.model.AuditLog;
import com.kota.model.User;
import com.kota.repository.AuditLogRepository;
import com.kota.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void log(String action, String entityType, Long entityId, String oldValue, String newValue,
                    String reason, String ipAddress, Long performedById) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setReason(reason);
        log.setIpAddress(ipAddress);
        if (performedById != null) {
            userRepository.findById(performedById).ifPresent(log::setPerformedBy);
        }
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditLogs(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
