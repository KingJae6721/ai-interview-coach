package com.aiinterview.interview.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InterviewQuestionResponse {

    private final Integer questionOrder;
    private final String content;

    @Builder
    public InterviewQuestionResponse(Integer questionOrder, String content) {
        this.questionOrder = questionOrder;
        this.content = content;
    }
}
