package com.aiinterview.interview.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InterviewCompleteUnavailableResponse {

    private final boolean allAnswered;
    private final long unansweredCount;
    private final Long nextQuestionId;

    @Builder
    public InterviewCompleteUnavailableResponse(boolean allAnswered, long unansweredCount, Long nextQuestionId) {
        this.allAnswered = allAnswered;
        this.unansweredCount = unansweredCount;
        this.nextQuestionId = nextQuestionId;
    }
}
