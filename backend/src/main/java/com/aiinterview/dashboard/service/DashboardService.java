package com.aiinterview.dashboard.service;

import com.aiinterview.dashboard.dto.DashboardSummaryResponse;
import com.aiinterview.dashboard.dto.DashboardScoreTrendResponse;
import com.aiinterview.dashboard.dto.DashboardAnalyticsPeriod;
import com.aiinterview.dashboard.dto.DashboardAnalyticsResponse;

import java.util.List;

public interface DashboardService {

    DashboardSummaryResponse getSummary(Long userId);

    List<DashboardScoreTrendResponse> getScoreTrend(Long userId, int limit);

    List<DashboardAnalyticsResponse> getAnalytics(Long userId, DashboardAnalyticsPeriod period, int limit);
}
