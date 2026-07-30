package com.aiinterview.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InterviewCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String title;
}
