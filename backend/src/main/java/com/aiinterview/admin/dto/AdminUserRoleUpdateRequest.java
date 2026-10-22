package com.aiinterview.admin.dto;

import com.aiinterview.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record AdminUserRoleUpdateRequest(
        @NotNull(message = "변경할 권한은 필수입니다.") UserRole role
) {
}
