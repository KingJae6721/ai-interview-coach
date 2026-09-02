package com.aiinterview.jobposting.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class JobPostingSummaryResponse {

    private final Long jobPostingId;
    private final String postingUrl;
    private final String companyName;
    private final String positionName;
    private final String summary;
    private final List<String> techStack;
    private final LocalDateTime analyzedAt;

    @Builder
    public JobPostingSummaryResponse(Long jobPostingId, String postingUrl, String companyName,
                                     String positionName, String summary, List<String> techStack,
                                     LocalDateTime analyzedAt) {
        this.jobPostingId = jobPostingId;
        this.postingUrl = postingUrl;
        this.companyName = companyName;
        this.positionName = positionName;
        this.summary = summary;
        this.techStack = List.copyOf(techStack);
        this.analyzedAt = analyzedAt;
    }
}
