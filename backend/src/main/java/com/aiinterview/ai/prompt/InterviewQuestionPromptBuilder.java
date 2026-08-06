package com.aiinterview.ai.prompt;

import com.aiinterview.ai.dto.InterviewQuestionDistribution;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.jobposition.entity.JobPosition;

import java.util.List;

public final class InterviewQuestionPromptBuilder {

    private static final String NOT_PROVIDED = "정보 없음";

    private InterviewQuestionPromptBuilder() {
    }

    public static String buildUserPrompt(Interview interview, List<InterviewQuestionDistribution> distributions) {
        JobPosition jobPosition = interview.getJobPosition();

        return """
                Interview title: %s
                Company: %s
                Job position: %s
                Required tech stack: %s
                Interview criteria: %s

                Required question distribution:
                %s

                Generate questions tailored to the company and job position context above. Follow the assigned order,
                category, and difficulty exactly. Do not repeat the same category focus or ask similar questions.
                """.formatted(
                interview.getTitle(),
                jobPosition.getCompany().getName(),
                jobPosition.getName(),
                formatTechStack(jobPosition.getTechStack()),
                getOrDefault(jobPosition.getInterviewCriteria()),
                formatDistributions(distributions)
        );
    }

    private static String formatTechStack(List<String> techStack) {
        return techStack == null || techStack.isEmpty() ? NOT_PROVIDED : String.join(", ", techStack);
    }

    private static String getOrDefault(String value) {
        return value == null || value.isBlank() ? NOT_PROVIDED : value;
    }

    private static String formatDistributions(List<InterviewQuestionDistribution> distributions) {
        return distributions.stream()
                .map(distribution -> "%d. %s / %s".formatted(
                        distribution.questionOrder(), distribution.category(), distribution.difficulty()))
                .collect(java.util.stream.Collectors.joining("\n"));
    }
}
