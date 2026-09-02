package com.aiinterview.dashboard.controller;

import com.aiinterview.auth.CustomUserDetails;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.dashboard.dto.DashboardSummaryResponse;
import com.aiinterview.dashboard.dto.DashboardScoreTrendResponse;
import com.aiinterview.dashboard.dto.DashboardAnalyticsPeriod;
import com.aiinterview.dashboard.dto.DashboardAnalyticsResponse;
import com.aiinterview.dashboard.dto.DashboardWeaknessResponse;
import com.aiinterview.dashboard.service.DashboardService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Validated
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        DashboardSummaryResponse response = dashboardService.getSummary(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/score-trend")
    public ResponseEntity<ApiResponse<List<DashboardScoreTrendResponse>>> getScoreTrend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "limit", defaultValue = "10") @Min(1) @Max(100) int limit) {

        List<DashboardScoreTrendResponse> response = dashboardService.getScoreTrend(userDetails.getId(), limit);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<List<DashboardAnalyticsResponse>>> getAnalytics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "period", defaultValue = "WEEKLY") DashboardAnalyticsPeriod period,
            @RequestParam(name = "limit", defaultValue = "6") @Min(1) @Max(24) int limit) {

        List<DashboardAnalyticsResponse> response = dashboardService.getAnalytics(userDetails.getId(), period, limit);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/weaknesses")
    public ResponseEntity<ApiResponse<DashboardWeaknessResponse>> getWeaknesses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        DashboardWeaknessResponse response = dashboardService.getWeaknesses(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
