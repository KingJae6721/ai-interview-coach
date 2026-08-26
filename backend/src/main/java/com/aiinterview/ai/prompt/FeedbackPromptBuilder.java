package com.aiinterview.ai.prompt;

import com.aiinterview.ai.dto.InterviewFeedbackRequest;

public final class FeedbackPromptBuilder {

    private static final String SYSTEM_PROMPT = """
            You are an expert technical interview coach. Evaluate the supplied interview answers objectively.
            Return feedback in Korean. Score overallScore from 0 to 100.
            strengths, weaknesses, improvementSuggestions, and summary must each be concise but actionable text.
            """;

    private FeedbackPromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public static String buildUserPrompt(InterviewFeedbackRequest request) {
        StringBuilder prompt = new StringBuilder("Interview title: ")
                .append(request.getInterviewTitle())
                .append("\nFeedback type: ")
                .append(request.isPartial() ? "partial" : "complete")
                .append("\nAnswered questions: ")
                .append(request.getAnsweredCount())
                .append(" / ")
                .append(request.getTotalQuestionCount())
                .append("\n\nQuestion and answer records:\n");

        if (request.isPartial()) {
            prompt.append("This is partial feedback. Do not infer performance from unanswered questions.\n\n");
        }

        for (InterviewFeedbackRequest.QuestionAnswer questionAnswer : request.getQuestionAnswers()) {
            prompt.append("[Question ")
                    .append(questionAnswer.getQuestionOrder())
                    .append("] ")
                    .append(questionAnswer.getQuestionContent())
                    .append("\n[Answer] ")
                    .append(questionAnswer.getAnswerContent())
                    .append("\n\n");
        }

        return prompt.toString();
    }
}
