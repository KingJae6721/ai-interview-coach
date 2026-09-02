package com.aiinterview.jobposting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JobPostingAnalyzeRequest {

    @Deprecated
    private Long jobPositionId;

    @NotBlank
    @Size(max = 2048)
    private String postingUrl;
}
