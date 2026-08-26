package com.aiinterview.ai.prompt;

public final class JobPostingAnalysisPromptBuilder {

    private JobPostingAnalysisPromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return """
                You extract facts from a job posting for interview preparation.
                Use only facts explicitly present in the supplied posting. Do not infer, supplement, or invent facts.
                Return Korean text when the source is Korean; otherwise preserve the source language.
                Use null for unavailable scalar fields and [] for unavailable list fields.
                """;
    }

    public static String buildUserPrompt(String extractedContent) {
        return """
                Analyze the following job posting text.

                --- JOB POSTING START ---
                %s
                --- JOB POSTING END ---
                """.formatted(extractedContent);
    }
}
