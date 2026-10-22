package com.aiinterview.admin.dto;

public record AdminUserInterviewStatistics(
        Long totalInterviews,
        Long readyInterviews,
        Long inProgressInterviews,
        Long completedInterviews,
        Long cancelledInterviews
) {
}
