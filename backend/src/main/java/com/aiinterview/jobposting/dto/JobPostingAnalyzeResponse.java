package com.aiinterview.jobposting.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class JobPostingAnalyzeResponse {

    private final Long jobPostingId;
    private final Long jobPositionId;
    private final String postingUrl;
    private final String title;
    private final String companyName;
    private final String positionName;
    private final List<String> responsibilities;
    private final List<String> requiredQualifications;
    private final List<String> preferredQualifications;
    private final List<String> techStack;
    private final List<String> experienceRequirements;
    private final List<String> keywords;
    private final String summary;
    private final LocalDateTime analyzedAt;

    @Builder
    public JobPostingAnalyzeResponse(Long jobPostingId, Long jobPositionId, String postingUrl, String title,
                                     String companyName, String positionName, List<String> responsibilities,
                                     List<String> requiredQualifications, List<String> preferredQualifications,
                                     List<String> techStack, List<String> experienceRequirements, List<String> keywords,
                                     String summary, LocalDateTime analyzedAt) {
        this.jobPostingId = jobPostingId;
        this.jobPositionId = jobPositionId;
        this.postingUrl = postingUrl;
        this.title = title;
        this.companyName = companyName;
        this.positionName = positionName;
        this.responsibilities = List.copyOf(responsibilities);
        this.requiredQualifications = List.copyOf(requiredQualifications);
        this.preferredQualifications = List.copyOf(preferredQualifications);
        this.techStack = List.copyOf(techStack);
        this.experienceRequirements = List.copyOf(experienceRequirements);
        this.keywords = List.copyOf(keywords);
        this.summary = summary;
        this.analyzedAt = analyzedAt;
    }
}
