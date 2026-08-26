package com.aiinterview.resume.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ResumeAnalyzeResponse {
    private final Long resumeId;
    private final String originalFileName;
    private final long fileSize;
    private final String summary;
    private final List<String> skills;
    private final List<String> workExperiences;
    private final List<String> projects;
    private final List<String> education;
    private final List<String> certifications;
    private final List<String> achievements;
    private final List<String> strengths;
    private final List<String> keywords;
    private final LocalDateTime analyzedAt;

    @Builder
    public ResumeAnalyzeResponse(Long resumeId, String originalFileName, long fileSize, String summary,
                                 List<String> skills, List<String> workExperiences, List<String> projects,
                                 List<String> education, List<String> certifications, List<String> achievements,
                                 List<String> strengths, List<String> keywords, LocalDateTime analyzedAt) {
        this.resumeId = resumeId;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.summary = summary;
        this.skills = List.copyOf(skills);
        this.workExperiences = List.copyOf(workExperiences);
        this.projects = List.copyOf(projects);
        this.education = List.copyOf(education);
        this.certifications = List.copyOf(certifications);
        this.achievements = List.copyOf(achievements);
        this.strengths = List.copyOf(strengths);
        this.keywords = List.copyOf(keywords);
        this.analyzedAt = analyzedAt;
    }
}
