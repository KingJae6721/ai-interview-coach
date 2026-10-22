package com.aiinterview.admin.dto;

import com.aiinterview.admin.entity.AdminAuditAction;
import com.aiinterview.admin.entity.AdminAuditLog;
import com.aiinterview.admin.entity.AdminAuditTargetType;

import java.time.LocalDateTime;

public record AdminAuditLogResponse(
        Long auditLogId,
        Long actorUserId,
        AdminAuditTargetType targetType,
        Long targetId,
        AdminAuditAction action,
        String beforeValue,
        String afterValue,
        LocalDateTime createdAt
) {
    public static AdminAuditLogResponse from(AdminAuditLog auditLog) {
        return new AdminAuditLogResponse(
                auditLog.getId(),
                auditLog.getActorUserId(),
                auditLog.getTargetType(),
                auditLog.getTargetId(),
                auditLog.getAction(),
                auditLog.getBeforeValue(),
                auditLog.getAfterValue(),
                auditLog.getCreatedAt());
    }
}
