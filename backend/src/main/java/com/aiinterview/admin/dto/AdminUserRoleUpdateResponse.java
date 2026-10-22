package com.aiinterview.admin.dto;

import com.aiinterview.user.entity.UserRole;

public record AdminUserRoleUpdateResponse(Long userId, UserRole role) {
}
