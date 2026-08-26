package com.aiinterview.interview.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InterviewStateResponse {

    private final Long interviewId;
    private final String title;
    private final InterviewStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime cancelledAt;
    private final Long jobPositionId;
    private final String positionName;
    private final String companyName;

    @Builder
    public InterviewStateResponse(Long interviewId, String title, InterviewStatus status, LocalDateTime createdAt,
                                  LocalDateTime startedAt, LocalDateTime completedAt, LocalDateTime cancelledAt,
                                  Long jobPositionId,
                                  String positionName, String companyName) {
        this.interviewId = interviewId;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.cancelledAt = cancelledAt;
        this.jobPositionId = jobPositionId;
        this.positionName = positionName;
        this.companyName = companyName;
    }
}
