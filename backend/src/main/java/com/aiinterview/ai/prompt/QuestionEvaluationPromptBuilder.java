package com.aiinterview.ai.prompt;

import com.aiinterview.ai.dto.QuestionEvaluationRequest;

public final class QuestionEvaluationPromptBuilder {

    private QuestionEvaluationPromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return """
                You are a calibrated interview-answer evaluator.

                First decide whether the candidate gave an answer that can actually be evaluated.
                Set sufficient=false when the response does not substantively answer the question, is meaningless,
                is unrelated, or only says that the candidate does not know. Do not award token partial credit in
                these cases. When sufficient=false, every criterion score must be 0.

                A short answer can still be sufficient when it directly and correctly answers the question. Never
                use character count alone. Evaluate only what the question asks. Do not penalize a technical answer
                merely because it has no personal experience example unless the question explicitly requests one.

                Apply this same 0-100 anchor to every criterion:
                0 = no substantive answer;
                1-20 = extremely limited but relevant content;
                21-40 = partial answer with major omissions;
                41-60 = basic answer lacking accuracy, specificity, or support;
                61-80 = appropriate and sufficiently supported answer;
                81-90 = accurate, specific, logical, and strong answer;
                91-100 = exceptionally complete, deep, and well-supported answer.

                Keep criterion scores deterministic for identical input. Textual feedback must agree with the scores.
                Write every textual field in Korean and do not invent facts not supported by the answer.
                """;
    }

    public static String buildUserPrompt(QuestionEvaluationRequest request) {
        String rubric = QuestionEvaluationPolicy.criteria(request.getCategory()).stream()
                .map(criterion -> "- %s (%d%%): %s".formatted(
                        criterion.key(), criterion.weight(), criterion.description()))
                .collect(java.util.stream.Collectors.joining("\n"));

        return """
                Question category: %s
                Question difficulty: %s
                Question: %s
                Candidate answer: %s

                Category rubric:
                %s

                Evaluate only with the listed criteria. Difficulty controls the expected depth, not whether unrelated
                requirements should be introduced. If sufficient=false, explain why the answer cannot be evaluated.
                """.formatted(
                request.getCategory() == null ? "UNSPECIFIED" : request.getCategory(),
                request.getDifficulty(),
                request.getQuestionContent(),
                request.getAnswerContent(),
                rubric
        );
    }
}
