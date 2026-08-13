package com.aiinterview.ai.prompt;

import com.aiinterview.ai.dto.QuestionEvaluationRequest;

public final class QuestionEvaluationPromptBuilder {

    private QuestionEvaluationPromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return """
                You are an expert technical interviewer evaluating one interview answer.
                Assess the answer fairly against the question, category, and difficulty.
                Write every textual field in Korean. Do not invent facts not supported by the answer.
                """;
    }

    public static String buildUserPrompt(QuestionEvaluationRequest request) {
        return """
                Question category: %s
                Question difficulty: %s
                Question: %s
                Candidate answer: %s
                """.formatted(
                request.getCategory(),
                request.getDifficulty(),
                request.getQuestionContent(),
                request.getAnswerContent()
        );
    }
}
