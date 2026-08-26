package com.aiinterview.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ResumeAnalysisResult {
    private final String summary;
    private final List<String> skills;
    private final List<String> workExperiences;
    private final List<String> projects;
    private final List<String> education;
    private final List<String> certifications;
    private final List<String> achievements;
    private final List<String> strengths;
    private final List<String> keywords;
    private final String aiModel;

    @Builder
    public ResumeAnalysisResult(String summary, List<String> skills, List<String> workExperiences,
                                List<String> projects, List<String> education, List<String> certifications,
                                List<String> achievements, List<String> strengths, List<String> keywords,
                                String aiModel) {
        this.summary = summary;
        this.skills = List.copyOf(skills);
        this.workExperiences = List.copyOf(workExperiences);
        this.projects = List.copyOf(projects);
        this.education = List.copyOf(education);
        this.certifications = List.copyOf(certifications);
        this.achievements = List.copyOf(achievements);
        this.strengths = List.copyOf(strengths);
        this.keywords = List.copyOf(keywords);
        this.aiModel = aiModel;
    }
}
