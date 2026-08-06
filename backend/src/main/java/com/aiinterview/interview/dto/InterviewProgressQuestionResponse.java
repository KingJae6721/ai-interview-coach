package com.aiinterview.interview.dto;

import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InterviewProgressQuestionResponse {

    private final Long questionId;
    private final Long parentQuestionId;
    private final Integer questionOrder;
    private final String content;
    private final InterviewQuestionCategory category;
    private final InterviewQuestionDifficulty difficulty;
    private final String answerContent;
    private final LocalDateTime answeredAt;

    @Builder
    public InterviewProgressQuestionResponse(Long questionId, Long parentQuestionId, Integer questionOrder,
                                             String content, InterviewQuestionCategory category,
                                             InterviewQuestionDifficulty difficulty, String answerContent,
                                             LocalDateTime answeredAt) {
        this.questionId = questionId;
        this.parentQuestionId = parentQuestionId;
        this.questionOrder = questionOrder;
        this.content = content;
        this.category = category;
        this.difficulty = difficulty;
        this.answerContent = answerContent;
        this.answeredAt = answeredAt;
    }
}
