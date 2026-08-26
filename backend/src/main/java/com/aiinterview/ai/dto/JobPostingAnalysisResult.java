package com.aiinterview.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class JobPostingAnalysisResult {

    private final String companyName;
    private final String positionName;
    private final List<String> responsibilities;
    private final List<String> requiredQualifications;
    private final List<String> preferredQualifications;
    private final List<String> techStack;
    private final List<String> experienceRequirements;
    private final List<String> keywords;
    private final String summary;
    private final String aiModel;

    @Builder
    public JobPostingAnalysisResult(String companyName, String positionName, List<String> responsibilities,
                                    List<String> requiredQualifications, List<String> preferredQualifications,
                                    List<String> techStack, List<String> experienceRequirements, List<String> keywords,
                                    String summary, String aiModel) {
        this.companyName = companyName;
        this.positionName = positionName;
        this.responsibilities = List.copyOf(responsibilities);
        this.requiredQualifications = List.copyOf(requiredQualifications);
        this.preferredQualifications = List.copyOf(preferredQualifications);
        this.techStack = List.copyOf(techStack);
        this.experienceRequirements = List.copyOf(experienceRequirements);
        this.keywords = List.copyOf(keywords);
        this.summary = summary;
        this.aiModel = aiModel;
    }
}
