package com.aiinterview.admin.dto;

public record AdminDashboardInterviewStatistics(
        Long totalInterviews,
        Long readyInterviews,
        Long inProgressInterviews,
        Long completedInterviews,
        Long cancelledInterviews,
        Long todayCreatedInterviews
) {
}
