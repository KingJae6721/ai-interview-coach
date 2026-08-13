package com.aiinterview.jobposition.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class JobPositionResponse {

    private final Long jobPositionId;
    private final String positionName;
    private final Long companyId;
    private final String companyName;
    private final List<String> techStack;

    @Builder
    private JobPositionResponse(
            Long jobPositionId,
            String positionName,
            Long companyId,
            String companyName,
            List<String> techStack) {
        this.jobPositionId = jobPositionId;
        this.positionName = positionName;
        this.companyId = companyId;
        this.companyName = companyName;
        this.techStack = techStack == null ? List.of() : List.copyOf(techStack);
    }
}
