package com.aiinterview.ai.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "groq", matchIfMissing = true)
public class GroqAiProvider extends AbstractOpenAiCompatibleProvider {

    public GroqAiProvider(
            @Value("${ai.groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${ai.groq.api-key:}") String apiKey,
            @Value("${ai.groq.model:openai/gpt-oss-20b}") String model,
            @Value("${ai.timeout-seconds:30}") long timeoutSeconds,
            @Value("${ai.rate-limit.max-retries:2}") int maxRateLimitRetries,
            @Value("${ai.rate-limit.initial-backoff-millis:1000}") long initialBackoffMillis,
            @Value("${ai.rate-limit.max-backoff-millis:60000}") long maxBackoffMillis) {
        super("groq", baseUrl, apiKey, model, timeoutSeconds, maxRateLimitRetries,
                initialBackoffMillis, maxBackoffMillis);
    }
}
