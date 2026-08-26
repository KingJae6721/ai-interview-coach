package com.aiinterview.interview.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InterviewCancelResponse {

    private final Long interviewId;
    private final InterviewStatus status;
    private final LocalDateTime cancelledAt;

    @Builder
    public InterviewCancelResponse(Long interviewId, InterviewStatus status, LocalDateTime cancelledAt) {
        this.interviewId = interviewId;
        this.status = status;
        this.cancelledAt = cancelledAt;
    }
}
