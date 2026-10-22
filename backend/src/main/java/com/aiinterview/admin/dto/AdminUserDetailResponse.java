package com.aiinterview.admin.dto;

import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;

import java.time.LocalDateTime;

public record AdminUserDetailResponse(
        Long userId,
        String email,
        String nickname,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt,
        AdminUserInterviewStatistics interviewStatistics,
        AdminLatestInterviewResponse latestInterview
) {
}
