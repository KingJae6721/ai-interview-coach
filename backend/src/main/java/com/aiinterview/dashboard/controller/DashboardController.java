package com.aiinterview.dashboard.controller;

import com.aiinterview.auth.CustomUserDetails;
import com.aiinterview.common.dto.ApiResponse;
import com.aiinterview.dashboard.dto.DashboardSummaryResponse;
import com.aiinterview.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        DashboardSummaryResponse response = dashboardService.getSummary(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
