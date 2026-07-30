package com.aiinterview.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InterviewAnswerCreateRequest {

    @NotBlank
    private String answerContent;
}
