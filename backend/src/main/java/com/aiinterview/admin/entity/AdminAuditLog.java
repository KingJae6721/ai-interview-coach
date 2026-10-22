package com.aiinterview.admin.entity;

import com.aiinterview.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin_audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private AdminAuditTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AdminAuditAction action;

    @Column(name = "before_value", nullable = false, length = 255)
    private String beforeValue;

    @Column(name = "after_value", nullable = false, length = 255)
    private String afterValue;

    @Builder
    private AdminAuditLog(Long actorUserId, AdminAuditTargetType targetType, Long targetId,
                          AdminAuditAction action, String beforeValue, String afterValue) {
        this.actorUserId = actorUserId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.action = action;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
    }
}
