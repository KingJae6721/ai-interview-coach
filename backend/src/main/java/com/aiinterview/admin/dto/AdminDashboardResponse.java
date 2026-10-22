package com.aiinterview.admin.dto;

import java.util.List;

public record AdminDashboardResponse(
        long totalUsers,
        long totalInterviews,
        long readyInterviews,
        long inProgressInterviews,
        long completedInterviews,
        long cancelledInterviews,
        long todayCreatedInterviews,
        List<AdminInterviewListResponse> recentInterviews
) {
}
