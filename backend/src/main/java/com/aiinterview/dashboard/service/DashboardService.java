package com.aiinterview.dashboard.service;

import com.aiinterview.dashboard.dto.DashboardSummaryResponse;

public interface DashboardService {

    DashboardSummaryResponse getSummary(Long userId);
}
