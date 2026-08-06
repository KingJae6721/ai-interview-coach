package com.aiinterview.ai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class InterviewFollowUpQuestionResponse {

    private final Long parentQuestionId;
    private final Long followUpQuestionId;
    private final String content;
    private final boolean created;

    @Builder
    public InterviewFollowUpQuestionResponse(Long parentQuestionId, Long followUpQuestionId,
                                             String content, boolean created) {
        this.parentQuestionId = parentQuestionId;
        this.followUpQuestionId = followUpQuestionId;
        this.content = content;
        this.created = created;
    }
}
