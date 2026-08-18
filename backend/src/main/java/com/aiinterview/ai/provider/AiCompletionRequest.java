package com.aiinterview.ai.provider;

import java.util.Map;

public record AiCompletionRequest(
        String systemPrompt,
        String userPrompt,
        Map<String, Object> responseFormat
) {
}
