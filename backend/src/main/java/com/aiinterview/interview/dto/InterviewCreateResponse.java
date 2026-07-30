package com.aiinterview.interview.dto;

import com.aiinterview.interview.entity.InterviewStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class InterviewCreateResponse {

    private final Long interviewId;
    private final InterviewStatus status;

    @Builder
    public InterviewCreateResponse(Long interviewId, InterviewStatus status) {
        this.interviewId = interviewId;
        this.status = status;
    }
}
