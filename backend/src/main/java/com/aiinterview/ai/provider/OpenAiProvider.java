package com.aiinterview.ai.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "openai")
public class OpenAiProvider extends AbstractOpenAiCompatibleProvider {

    public OpenAiProvider(
            @Value("${ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${ai.openai.api-key:}") String apiKey,
            @Value("${ai.openai.model:gpt-4o-mini}") String model,
            @Value("${ai.timeout-seconds:30}") long timeoutSeconds,
            @Value("${ai.rate-limit.max-retries:2}") int maxRateLimitRetries,
            @Value("${ai.rate-limit.initial-backoff-millis:1000}") long initialBackoffMillis,
            @Value("${ai.rate-limit.max-backoff-millis:60000}") long maxBackoffMillis) {
        super("openai", baseUrl, apiKey, model, timeoutSeconds, maxRateLimitRetries,
                initialBackoffMillis, maxBackoffMillis);
    }
}
