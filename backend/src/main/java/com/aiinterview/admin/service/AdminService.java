package com.aiinterview.admin.service;

import com.aiinterview.admin.dto.AdminAuditLogResponse;
import com.aiinterview.admin.dto.AdminDashboardResponse;
import com.aiinterview.admin.dto.AdminInterviewDetailResponse;
import com.aiinterview.admin.dto.AdminInterviewListResponse;
import com.aiinterview.admin.dto.AdminUserDetailResponse;
import com.aiinterview.admin.dto.AdminUserListResponse;
import com.aiinterview.admin.dto.AdminUserRoleUpdateResponse;
import com.aiinterview.admin.entity.AdminAuditAction;
import com.aiinterview.admin.entity.AdminAuditTargetType;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AdminService {

    Page<AdminUserListResponse> getUsers(String email, UserRole role, UserStatus status, Pageable pageable);

    AdminUserDetailResponse getUser(Long userId);

    AdminUserRoleUpdateResponse updateUserRole(Long actorUserId, Long targetUserId, UserRole role);

    Page<AdminInterviewListResponse> getInterviews(
            InterviewStatus status, String userEmail, Pageable pageable);

    AdminInterviewDetailResponse getInterview(Long interviewId);

    AdminDashboardResponse getDashboard();

    Page<AdminAuditLogResponse> getAuditLogs(
            AdminAuditAction action,
            Long actorUserId,
            AdminAuditTargetType targetType,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable);
}
