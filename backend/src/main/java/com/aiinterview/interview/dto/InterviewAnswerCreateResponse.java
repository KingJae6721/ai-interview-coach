package com.aiinterview.interview.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InterviewAnswerCreateResponse {

    private final Long answerId;
    private final Long questionId;
    private final String answerContent;
    private final LocalDateTime answeredAt;
    private final boolean created;

    @Builder
    public InterviewAnswerCreateResponse(Long answerId, Long questionId, String answerContent,
                                         LocalDateTime answeredAt, boolean created) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.answerContent = answerContent;
        this.answeredAt = answeredAt;
        this.created = created;
    }
}
