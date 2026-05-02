package com.kota.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuditLogResponse {

    private Long id;
    private String action;
    private String entityType;
    private String oldValue;
    private String newValue;
    private String reason;
    private String performedBy;
    private LocalDateTime createdAt;
}
