import type {
  DashboardAnalyticsPeriod,
  DashboardAnalyticsResponse,
  DashboardScoreTrendResponse,
  DashboardSummaryResponse,
  DashboardWeaknessResponse,
} from "@/features/dashboard/types/dashboard";
import { apiFetch } from "@/services/api-client";

export function getDashboardSummary(): Promise<DashboardSummaryResponse> {
  return apiFetch<DashboardSummaryResponse>("/api/v1/dashboard/summary");
}

export function getDashboardScoreTrend(
  limit = 10,
): Promise<DashboardScoreTrendResponse[]> {
  return apiFetch<DashboardScoreTrendResponse[]>(
    `/api/v1/dashboard/score-trend?limit=${limit}`,
  );
}

export function getDashboardAnalytics(
  period: DashboardAnalyticsPeriod,
  limit = 6,
): Promise<DashboardAnalyticsResponse[]> {
  const searchParams = new URLSearchParams({
    period,
    limit: String(limit),
  });

  return apiFetch<DashboardAnalyticsResponse[]>(
    `/api/v1/dashboard/analytics?${searchParams.toString()}`,
  );
}

export function getDashboardWeaknesses(): Promise<DashboardWeaknessResponse> {
  return apiFetch<DashboardWeaknessResponse>("/api/v1/dashboard/weaknesses");
}
