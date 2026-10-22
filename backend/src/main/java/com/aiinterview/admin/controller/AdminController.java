package com.aiinterview.admin.controller;

import com.aiinterview.admin.dto.AdminAuditLogResponse;
import com.aiinterview.admin.dto.AdminDashboardResponse;
import com.aiinterview.admin.dto.AdminInterviewDetailResponse;
import com.aiinterview.admin.dto.AdminInterviewListResponse;
import com.aiinterview.admin.dto.AdminUserDetailResponse;
import com.aiinterview.admin.dto.AdminUserListResponse;
import com.aiinterview.admin.dto.AdminUserRoleUpdateRequest;
import com.aiinterview.admin.dto.AdminUserRoleUpdateResponse;
import com.aiinterview.admin.entity.AdminAuditAction;
import com.aiinterview.admin.entity.AdminAuditTargetType;
import com.aiinterview.admin.service.AdminService;
import com.aiinterview.auth.CustomUserDetails;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminUserListResponse>>> getUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUsers(email, role, status, pageable)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUser(
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUser(userId)));
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse<AdminUserRoleUpdateResponse>> updateUserRole(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("userId") Long userId,
            @RequestBody @Valid AdminUserRoleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.updateUserRole(userDetails.getId(), userId, request.role())));
    }

    @GetMapping("/interviews")
    public ResponseEntity<ApiResponse<Page<AdminInterviewListResponse>>> getInterviews(
            @RequestParam(required = false) InterviewStatus status,
            @RequestParam(required = false) String userEmail,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.getInterviews(status, userEmail, pageable)));
    }

    @GetMapping("/interviews/{interviewId}")
    public ResponseEntity<ApiResponse<AdminInterviewDetailResponse>> getInterview(
            @PathVariable("interviewId") Long interviewId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getInterview(interviewId)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboard()));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AdminAuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) AdminAuditAction action,
            @RequestParam(required = false) Long actorUserId,
            @RequestParam(required = false) AdminAuditTargetType targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAuditLogs(
                action, actorUserId, targetType, fromDate, toDate, pageable)));
    }
}
