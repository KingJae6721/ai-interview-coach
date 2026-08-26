package com.aiinterview.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class DashboardSummaryResponse {

    private final long totalInterviews;
    private final long completedInterviews;
    private final long cancelledInterviews;
    private final Double averageScore;
    private final Integer highestScore;
    private final LocalDateTime latestInterviewAt;
    private final List<DashboardRecentInterviewResponse> recentInterviews;

    @Builder
    public DashboardSummaryResponse(long totalInterviews, long completedInterviews, long cancelledInterviews,
                                    Double averageScore,
                                    Integer highestScore, LocalDateTime latestInterviewAt,
                                    List<DashboardRecentInterviewResponse> recentInterviews) {
        this.totalInterviews = totalInterviews;
        this.completedInterviews = completedInterviews;
        this.cancelledInterviews = cancelledInterviews;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.latestInterviewAt = latestInterviewAt;
        this.recentInterviews = recentInterviews;
    }
}
