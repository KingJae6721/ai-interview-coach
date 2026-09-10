package com.aiinterview.ai.prompt;

import com.aiinterview.ai.dto.QuestionEvaluationRequest;

public final class QuestionEvaluationPromptBuilder {

    private QuestionEvaluationPromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return """
                Evaluate one interview answer using only the supplied question, answer, category, difficulty, and rubric.
                sufficient=false for meaningless, unrelated, no-knowledge, or non-responsive answers; then all scores=0.
                Short but substantively correct answers can be sufficient; never judge by length alone.
                Do not require experience unless the question asks for it or the rubric contains it.
                Difficulty sets expected depth only; do not introduce requirements unrelated to the question.
                Score anchor: 0 none; 1-20 extremely limited; 21-40 major omissions; 41-60 basic but weak;
                61-80 sufficient; 81-90 strong; 91-100 exceptional.
                Be deterministic, keep feedback consistent with scores, use Korean, and invent no facts.
                """;
    }

    public static String buildUserPrompt(QuestionEvaluationRequest request) {
        String rubric = QuestionEvaluationPolicy.criteria(request.getCategory()).stream()
                .map(criterion -> "%s|%d|%s".formatted(
                        criterion.key(), criterion.weight(), criterion.description()))
                .collect(java.util.stream.Collectors.joining(";"));

        return """
                category=%s
                difficulty=%s
                rubric=key|weightPercent|criterion:%s
                question=%s
                answer=%s
                """.formatted(
                request.getCategory() == null ? "UNSPECIFIED" : request.getCategory(),
                request.getDifficulty(),
                rubric,
                request.getQuestionContent(),
                request.getAnswerContent()
        );
    }
}
