package com.aiinterview.ai.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "groq", matchIfMissing = true)
public class GroqAiProvider extends AbstractOpenAiCompatibleProvider {

    private final String reasoningEffort;

    public GroqAiProvider(
            @Value("${ai.groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${ai.groq.api-key:}") String apiKey,
            @Value("${ai.groq.model:openai/gpt-oss-20b}") String model,
            @Value("${ai.timeout-seconds:30}") long timeoutSeconds,
            @Value("${ai.rate-limit.max-retries:2}") int maxRateLimitRetries,
            @Value("${ai.rate-limit.initial-backoff-millis:1000}") long initialBackoffMillis,
            @Value("${ai.rate-limit.max-backoff-millis:60000}") long maxBackoffMillis,
            @Value("${ai.groq.reasoning-effort:low}") String reasoningEffort) {
        super("groq", baseUrl, apiKey, model, timeoutSeconds, maxRateLimitRetries,
                initialBackoffMillis, maxBackoffMillis);
        this.reasoningEffort = reasoningEffort;
    }

    @Override
    protected void customizeRequestBody(Map<String, Object> body) {
        if (StringUtils.hasText(reasoningEffort)) {
            body.put("reasoning_effort", reasoningEffort);
        }
    }
}
