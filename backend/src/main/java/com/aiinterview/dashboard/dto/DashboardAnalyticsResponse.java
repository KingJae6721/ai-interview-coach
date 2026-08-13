package com.aiinterview.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DashboardAnalyticsResponse {

    private final LocalDateTime periodStartAt;
    private final Double averageScore;
    private final long interviewCount;
    private final Double scoreChange;

    @Builder
    public DashboardAnalyticsResponse(LocalDateTime periodStartAt, Double averageScore,
                                      long interviewCount, Double scoreChange) {
        this.periodStartAt = periodStartAt;
        this.averageScore = averageScore;
        this.interviewCount = interviewCount;
        this.scoreChange = scoreChange;
    }
}
