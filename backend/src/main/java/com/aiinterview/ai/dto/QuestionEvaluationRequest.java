package com.aiinterview.ai.dto;

import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import lombok.Builder;
import lombok.Getter;

@Getter
public class QuestionEvaluationRequest {

    private final String questionContent;
    private final String answerContent;
    private final InterviewQuestionCategory category;
    private final InterviewQuestionDifficulty difficulty;

    @Builder
    public QuestionEvaluationRequest(String questionContent, String answerContent,
                                     InterviewQuestionCategory category, InterviewQuestionDifficulty difficulty) {
        this.questionContent = questionContent;
        this.answerContent = answerContent;
        this.category = category;
        this.difficulty = difficulty;
    }
}
