package com.aiinterview.admin.dto;

import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;

import java.time.LocalDateTime;

public record AdminUserListResponse(
        Long userId,
        String email,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        Long interviewCount
) {
}
