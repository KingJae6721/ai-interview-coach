package com.aiinterview.admin.repository;

import com.aiinterview.admin.entity.AdminAuditAction;
import com.aiinterview.admin.entity.AdminAuditLog;
import com.aiinterview.admin.entity.AdminAuditTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    @Query("""
            select auditLog
            from AdminAuditLog auditLog
            where (:action is null or auditLog.action = :action)
              and (:actorUserId is null or auditLog.actorUserId = :actorUserId)
              and (:targetType is null or auditLog.targetType = :targetType)
              and (:fromDateTime is null or auditLog.createdAt >= :fromDateTime)
              and (:toDateTime is null or auditLog.createdAt < :toDateTime)
            """)
    Page<AdminAuditLog> findAdminAuditLogs(
            @Param("action") AdminAuditAction action,
            @Param("actorUserId") Long actorUserId,
            @Param("targetType") AdminAuditTargetType targetType,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            Pageable pageable);
}
