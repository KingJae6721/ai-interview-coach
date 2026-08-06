package com.aiinterview.feedback.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InterviewResultQuestionAnswerResponse {

    private final Integer questionOrder;
    private final String questionContent;
    private final String answerContent;
    private final LocalDateTime answeredAt;

    @Builder
    public InterviewResultQuestionAnswerResponse(Integer questionOrder, String questionContent,
                                                 String answerContent, LocalDateTime answeredAt) {
        this.questionOrder = questionOrder;
        this.questionContent = questionContent;
        this.answerContent = answerContent;
        this.answeredAt = answeredAt;
    }
}
