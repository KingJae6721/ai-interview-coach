package com.aiinterview.ai.prompt;

import com.aiinterview.ai.dto.InterviewQuestionDistribution;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.jobposting.entity.JobPostingAnalysis;

import java.util.List;

public final class InterviewQuestionPromptBuilder {

    private static final String NOT_PROVIDED = "정보 없음";

    private InterviewQuestionPromptBuilder() {
    }

    public static String buildUserPrompt(Interview interview, List<InterviewQuestionDistribution> distributions) {
        return buildUserPrompt(interview, null, distributions);
    }

    public static String buildUserPrompt(Interview interview, JobPostingAnalysis jobPostingAnalysis,
                                         List<InterviewQuestionDistribution> distributions) {
        JobPosition jobPosition = interview.getJobPosition();

        return """
                Interview title: %s
                Company: %s
                Job position: %s
                Required tech stack: %s
                Interview criteria: %s
                %s

                Required question distribution:
                %s

                Generate questions tailored to the company and job position context above. Follow the assigned order,
                category, and difficulty exactly. When job posting analysis is provided, reflect its emphasized
                requirements while balancing role fundamentals with posting-specific questions. Do not merely copy
                posting sentences or ask questions that only list technology names. Prefer questions that reveal
                practical experience and problem-solving. Do not assume facts absent from the context, and do not ask
                duplicate or substantially similar questions.
                """.formatted(
                interview.getTitle(),
                jobPosition.getCompany().getName(),
                jobPosition.getName(),
                formatTechStack(jobPosition.getTechStack()),
                getOrDefault(jobPosition.getInterviewCriteria()),
                formatJobPostingAnalysis(jobPostingAnalysis),
                formatDistributions(distributions)
        );
    }

    private static String formatJobPostingAnalysis(JobPostingAnalysis analysis) {
        if (analysis == null) {
            return "Job posting analysis: " + NOT_PROVIDED;
        }
        return """
                Job posting analysis:
                - Summary: %s
                - Responsibilities: %s
                - Required qualifications: %s
                - Preferred qualifications: %s
                - Tech stack: %s
                - Experience requirements: %s
                - Keywords: %s
                """.formatted(
                getOrDefault(analysis.getSummary()),
                formatList(analysis.getResponsibilities()),
                formatList(analysis.getRequiredQualifications()),
                formatList(analysis.getPreferredQualifications()),
                formatList(analysis.getTechStack()),
                formatList(analysis.getExperienceRequirements()),
                formatList(analysis.getKeywords())
        );
    }

    private static String formatTechStack(List<String> techStack) {
        return formatList(techStack);
    }

    private static String formatList(List<String> values) {
        return values == null || values.isEmpty() ? NOT_PROVIDED : String.join(", ", values);
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
