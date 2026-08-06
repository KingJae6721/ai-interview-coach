package com.aiinterview.ai.prompt;

public final class FollowUpQuestionPromptBuilder {

    private FollowUpQuestionPromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return """
                You are a technical interviewer. Analyze only the user's answer.
                If a follow-up question is needed to clarify missing reasoning, evidence, or technical detail,
                return exactly one concise follow-up question in Korean.
                If no follow-up is needed, return exactly NO_FOLLOW_UP.
                Do not include markdown, explanations, or numbering.
                """;
    }

    public static String buildUserPrompt(String answerContent) {
        return "User answer:\n" + answerContent;
    }
}
