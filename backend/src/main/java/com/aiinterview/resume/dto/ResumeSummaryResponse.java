package com.aiinterview.resume.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ResumeSummaryResponse {
    private final Long resumeId;
    private final String originalFileName;
    private final LocalDateTime createdAt;
    private final String summary;
    private final List<String> skills;

    @Builder
    public ResumeSummaryResponse(Long resumeId, String originalFileName, LocalDateTime createdAt,
                                 String summary, List<String> skills) {
        this.resumeId = resumeId;
        this.originalFileName = originalFileName;
        this.createdAt = createdAt;
        this.summary = summary;
        this.skills = List.copyOf(skills);
    }
}
