import type { UserRole } from "@/features/auth/types/auth";
import type {
  AdminAuditLog,
  AdminAuditLogFilters,
  AdminDashboard,
  AdminInterviewDetail,
  AdminInterviewFilters,
  AdminInterviewListItem,
  AdminUserDetail,
  AdminUserFilters,
  AdminUserListItem,
  PageResponse,
} from "@/features/admin/types/admin";
import { apiFetch } from "@/services/api-client";

function toQuery(params: object): string {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== "") query.set(key, String(value));
  });
  const value = query.toString();
  return value ? `?${value}` : "";
}

export function getAdminDashboard(): Promise<AdminDashboard> {
  return apiFetch("/api/v1/admin/dashboard");
}

export function getAdminUsers(
  filters: AdminUserFilters,
): Promise<PageResponse<AdminUserListItem>> {
  return apiFetch(`/api/v1/admin/users${toQuery(filters)}`);
}

export function getAdminUser(userId: number): Promise<AdminUserDetail> {
  return apiFetch(`/api/v1/admin/users/${userId}`);
}

export function updateAdminUserRole(
  userId: number,
  role: UserRole,
): Promise<{ userId: number; role: UserRole }> {
  return apiFetch(`/api/v1/admin/users/${userId}/role`, {
    method: "PATCH",
    body: JSON.stringify({ role }),
  });
}

export function getAdminInterviews(
  filters: AdminInterviewFilters,
): Promise<PageResponse<AdminInterviewListItem>> {
  return apiFetch(`/api/v1/admin/interviews${toQuery(filters)}`);
}

export function getAdminInterview(
  interviewId: number,
): Promise<AdminInterviewDetail> {
  return apiFetch(`/api/v1/admin/interviews/${interviewId}`);
}

export function getAdminAuditLogs(
  filters: AdminAuditLogFilters,
): Promise<PageResponse<AdminAuditLog>> {
  return apiFetch(`/api/v1/admin/audit-logs${toQuery(filters)}`);
}
