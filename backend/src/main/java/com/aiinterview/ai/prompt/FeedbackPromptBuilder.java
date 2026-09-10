package com.aiinterview.ai.prompt;

import com.aiinterview.ai.dto.InterviewFeedbackRequest;

public final class FeedbackPromptBuilder {

    private static final String SYSTEM_PROMPT = """
            Evaluate only the supplied interview answers. Return concise, actionable Korean feedback.
            Score overallScore 0-100. Do not infer facts or performance from unanswered questions.
            """;

    private FeedbackPromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public static String buildUserPrompt(InterviewFeedbackRequest request) {
        StringBuilder prompt = new StringBuilder("title=")
                .append(request.getInterviewTitle())
                .append("\ntype=")
                .append(request.isPartial() ? "partial" : "complete")
                .append("\nanswered=")
                .append(request.getAnsweredCount())
                .append('/')
                .append(request.getTotalQuestionCount())
                .append("\nrecords:\n");

        for (InterviewFeedbackRequest.QuestionAnswer questionAnswer : request.getQuestionAnswers()) {
            prompt.append("Q")
                    .append(questionAnswer.getQuestionOrder())
                    .append(':')
                    .append(questionAnswer.getQuestionContent())
                    .append("\nA:")
                    .append(questionAnswer.getAnswerContent())
                    .append('\n');
        }

        return prompt.toString();
    }
}
