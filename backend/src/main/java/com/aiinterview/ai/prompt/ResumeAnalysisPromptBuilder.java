package com.aiinterview.ai.prompt;

public final class ResumeAnalysisPromptBuilder {
    private ResumeAnalysisPromptBuilder() {
    }

    public static String buildSystemPrompt() {
        return """
                Extract structured facts from the supplied resume. Use only information explicitly present in the text.
                Do not infer missing experience, skills, achievements, or hiring suitability. Preserve quantitative
                achievements. Separate work experience, projects, education, certifications, and achievements.
                Use null for an unavailable summary and [] for unavailable list fields.
                """;
    }

    public static String buildUserPrompt(String extractedText) {
        return """
                Analyze the following resume text.
                --- RESUME START ---
                %s
                --- RESUME END ---
                """.formatted(extractedText);
    }
}
