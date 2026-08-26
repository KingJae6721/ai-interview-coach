package com.aiinterview.ai.prompt;

import com.aiinterview.ai.dto.InterviewQuestionDistribution;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.jobposting.entity.JobPostingAnalysis;
import com.aiinterview.resume.entity.ResumeAnalysis;

import java.util.List;
import java.util.regex.Pattern;

public final class InterviewQuestionPromptBuilder {

    private static final String NOT_PROVIDED = "정보 없음";
    private static final String REDACTED = "[REDACTED]";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<![\\p{L}\\d])(?:\\+?\\d{1,3}[\\s.-]?)?(?:\\(?\\d{2,4}\\)?[\\s.-]?){2,4}\\d{3,4}(?![\\p{L}\\d])");

    private InterviewQuestionPromptBuilder() {
    }

    public static String buildUserPrompt(Interview interview, List<InterviewQuestionDistribution> distributions) {
        return buildUserPrompt(interview, null, null, distributions);
    }

    public static String buildUserPrompt(Interview interview, JobPostingAnalysis jobPostingAnalysis,
                                         List<InterviewQuestionDistribution> distributions) {
        return buildUserPrompt(interview, jobPostingAnalysis, null, distributions);
    }

    public static String buildUserPrompt(Interview interview, JobPostingAnalysis jobPostingAnalysis,
                                         ResumeAnalysis resumeAnalysis,
                                         List<InterviewQuestionDistribution> distributions) {
        JobPosition jobPosition = interview.getJobPosition();

        return """
                [JOB POSITION CONTEXT]
                - Interview title: %s
                - Company: %s
                - Job position: %s
                - Required tech stack: %s
                - Interview criteria: %s
                %s
                %s

                [QUESTION GENERATION RULES]
                Required question distribution:
                %s

                - Follow the assigned order, category, and difficulty exactly.
                - Balance role fundamentals with any provided job posting and resume context. Do not turn every
                  question into a posting-only or resume-only question.
                - When resume context is provided, ask about documented experience, personal contribution, decisions,
                  actions, outcomes, measurements, technology trade-offs, and practical problem-solving. Never assume
                  experience absent from the resume, imply that the candidate is exaggerating, or ask for personal data.
                - When both job posting and resume context are provided, cross-reference them. For matching experience,
                  verify depth and application. When a requirement is not explicitly confirmed by the resume, ask how
                  transferable experience could be applied without claiming that the candidate lacks the requirement.
                - Do not merely copy context sentences or ask questions that only list technology names.
                - Do not assume facts absent from the context, and do not ask duplicate or substantially similar questions.
                """.formatted(
                interview.getTitle(),
                jobPosition.getCompany().getName(),
                jobPosition.getName(),
                formatTechStack(jobPosition.getTechStack()),
                getOrDefault(jobPosition.getInterviewCriteria()),
                formatJobPostingAnalysis(jobPostingAnalysis),
                formatResumeAnalysis(resumeAnalysis),
                formatDistributions(distributions)
        );
    }

    private static String formatJobPostingAnalysis(JobPostingAnalysis analysis) {
        if (analysis == null) {
            return "";
        }
        return """
                [JOB POSTING CONTEXT]
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

    private static String formatResumeAnalysis(ResumeAnalysis analysis) {
        if (analysis == null) {
            return "";
        }
        return """
                [RESUME CONTEXT]
                - Summary: %s
                - Skills: %s
                - Work experiences: %s
                - Projects: %s
                - Education: %s
                - Certifications: %s
                - Achievements: %s
                - Strengths: %s
                - Keywords: %s
                """.formatted(
                sanitizePersonalInformation(getOrDefault(analysis.getSummary())),
                formatResumeList(analysis.getSkills()),
                formatResumeList(analysis.getWorkExperiences()),
                formatResumeList(analysis.getProjects()),
                formatResumeList(analysis.getEducation()),
                formatResumeList(analysis.getCertifications()),
                formatResumeList(analysis.getAchievements()),
                formatResumeList(analysis.getStrengths()),
                formatResumeList(analysis.getKeywords())
        );
    }

    private static String formatTechStack(List<String> techStack) {
        return formatList(techStack);
    }

    private static String formatList(List<String> values) {
        return values == null || values.isEmpty() ? NOT_PROVIDED : String.join(", ", values);
    }

    private static String formatResumeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return NOT_PROVIDED;
        }
        return values.stream()
                .map(InterviewQuestionPromptBuilder::sanitizePersonalInformation)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private static String sanitizePersonalInformation(String value) {
        String withoutEmail = EMAIL_PATTERN.matcher(value).replaceAll(REDACTED);
        return PHONE_PATTERN.matcher(withoutEmail).replaceAll(REDACTED);
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
