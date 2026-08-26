package com.aiinterview.interview.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InterviewHistoryResponse {

    private final Long interviewId;
    private final String title;
    private final InterviewStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime cancelledAt;
    private final String companyName;
    private final String positionName;
    private final Integer overallScore;
    private final boolean feedbackExists;
    private final boolean partial;

    @Builder
    public InterviewHistoryResponse(Long interviewId, String title, InterviewStatus status,
                                    LocalDateTime createdAt, LocalDateTime startedAt, LocalDateTime completedAt,
                                    LocalDateTime cancelledAt, String companyName, String positionName,
                                    Integer overallScore, boolean feedbackExists, boolean partial) {
        this.interviewId = interviewId;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.cancelledAt = cancelledAt;
        this.companyName = companyName;
        this.positionName = positionName;
        this.overallScore = overallScore;
        this.feedbackExists = feedbackExists;
        this.partial = partial;
    }
}
