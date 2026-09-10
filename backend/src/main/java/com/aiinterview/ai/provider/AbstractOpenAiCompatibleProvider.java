package com.aiinterview.ai.provider;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
abstract class AbstractOpenAiCompatibleProvider implements AiProvider {

    private static final int MAX_PROVIDER_ERROR_MESSAGE_LENGTH = 500;
    private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile(
            "\\\"message\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<![\\p{L}\\d])(?:\\+?\\d{1,3}[\\s.-]?)?(?:\\(?\\d{2,4}\\)?[\\s.-]?){2,4}\\d{3,4}(?![\\p{L}\\d])");
    private static final Pattern RESET_DURATION_PATTERN = Pattern.compile(
            "(?:(\\d+(?:\\.\\d+)?)h)?(?:(\\d+(?:\\.\\d+)?)m)?(?:(\\d+(?:\\.\\d+)?)s)?");

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String providerName;
    private final int maxRateLimitRetries;
    private final long initialBackoffMillis;
    private final long maxBackoffMillis;
    private final Sleeper sleeper;

    protected AbstractOpenAiCompatibleProvider(String providerName, String baseUrl, String apiKey,
                                               String model, long timeoutSeconds, int maxRateLimitRetries,
                                               long initialBackoffMillis, long maxBackoffMillis) {
        this(providerName, baseUrl, apiKey, model, timeoutSeconds, maxRateLimitRetries,
                initialBackoffMillis, maxBackoffMillis, Thread::sleep);
    }

    AbstractOpenAiCompatibleProvider(String providerName, String baseUrl, String apiKey,
                                     String model, long timeoutSeconds, int maxRateLimitRetries,
                                     long initialBackoffMillis, long maxBackoffMillis, Sleeper sleeper) {
        this(providerName, buildRestClient(baseUrl, apiKey, timeoutSeconds), apiKey, model,
                maxRateLimitRetries, initialBackoffMillis, maxBackoffMillis, sleeper);
    }

    AbstractOpenAiCompatibleProvider(String providerName, RestClient restClient, String apiKey, String model,
                                     int maxRateLimitRetries, long initialBackoffMillis,
                                     long maxBackoffMillis, Sleeper sleeper) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.model = model;
        this.providerName = providerName;
        this.maxRateLimitRetries = Math.max(0, maxRateLimitRetries);
        this.initialBackoffMillis = Math.max(1, initialBackoffMillis);
        this.maxBackoffMillis = Math.max(this.initialBackoffMillis, maxBackoffMillis);
        this.sleeper = sleeper;
    }

    private static RestClient buildRestClient(String baseUrl, String apiKey, long timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public String complete(AiCompletionRequest request) {
        if (!StringUtils.hasText(apiKey)) {
            log.error("AI provider request failed. provider={}, reason=API_KEY_MISSING", providerName);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", request.systemPrompt()),
                Map.of("role", "user", "content", request.userPrompt())
        ));
        if (request.responseFormat() != null) {
            body.put("response_format", request.responseFormat());
        }
        if (request.maxCompletionTokens() != null) {
            body.put("max_completion_tokens", request.maxCompletionTokens());
        }

        for (int retryCount = 0; ; retryCount++) {
            try {
                return restClient.post()
                        .uri("/chat/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);
            } catch (RestClientResponseException e) {
                if (e.getStatusCode().value() == 429 && retryCount < maxRateLimitRetries) {
                    RetryDelay retryDelay = resolveRetryDelay(e, retryCount);
                    log.warn("AI provider rate limited. provider={}, model={}, retry={}/{}, delayMs={}, delaySource={}",
                            providerName, model, retryCount + 1, maxRateLimitRetries,
                            retryDelay.millis(), retryDelay.source());
                    sleepBeforeRetry(retryDelay.millis());
                    continue;
                }
                log.error("AI provider request failed. provider={}, model={}, reason={}, status={}, providerMessage={}",
                        providerName, model, toFailureReason(e.getStatusCode().value()), e.getStatusCode(),
                        extractProviderErrorMessage(e.getResponseBodyAsString()));
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
            } catch (ResourceAccessException e) {
                log.error("AI provider request failed. provider={}, reason={}", providerName,
                        hasTimeoutCause(e) ? "TIMEOUT" : "NETWORK_ERROR");
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
            } catch (RestClientException e) {
                log.error("AI provider request failed. provider={}, reason=CLIENT_ERROR, errorType={}", providerName,
                        e.getClass().getSimpleName());
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
            }
        }
    }

    private RetryDelay resolveRetryDelay(RestClientResponseException exception, int retryCount) {
        String retryAfter = exception.getResponseHeaders() == null
                ? null : exception.getResponseHeaders().getFirst("Retry-After");
        Long retryAfterMillis = parseSeconds(retryAfter);
        if (retryAfterMillis != null) {
            return new RetryDelay(bound(retryAfterMillis), "retry-after");
        }

        String tokenReset = exception.getResponseHeaders() == null
                ? null : exception.getResponseHeaders().getFirst("x-ratelimit-reset-tokens");
        Long tokenResetMillis = parseDuration(tokenReset);
        if (tokenResetMillis != null) {
            return new RetryDelay(bound(tokenResetMillis), "x-ratelimit-reset-tokens");
        }

        long multiplier = 1L << Math.min(retryCount, 20);
        long fallback = initialBackoffMillis > Long.MAX_VALUE / multiplier
                ? maxBackoffMillis : initialBackoffMillis * multiplier;
        return new RetryDelay(bound(fallback), "exponential-backoff");
    }

    private Long parseSeconds(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim())
                    .multiply(BigDecimal.valueOf(1_000))
                    .setScale(0, RoundingMode.CEILING)
                    .longValueExact();
        } catch (ArithmeticException | NumberFormatException ignored) {
            return null;
        }
    }

    private Long parseDuration(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        Matcher matcher = RESET_DURATION_PATTERN.matcher(value.trim());
        if (!matcher.matches() || matcher.group(0).isEmpty()) {
            return null;
        }
        BigDecimal seconds = BigDecimal.ZERO;
        if (matcher.group(1) != null) {
            seconds = seconds.add(new BigDecimal(matcher.group(1)).multiply(BigDecimal.valueOf(3_600)));
        }
        if (matcher.group(2) != null) {
            seconds = seconds.add(new BigDecimal(matcher.group(2)).multiply(BigDecimal.valueOf(60)));
        }
        if (matcher.group(3) != null) {
            seconds = seconds.add(new BigDecimal(matcher.group(3)));
        }
        return seconds.multiply(BigDecimal.valueOf(1_000))
                .setScale(0, RoundingMode.CEILING)
                .longValue();
    }

    private long bound(long delayMillis) {
        return Math.min(Math.max(delayMillis, 1), maxBackoffMillis);
    }

    private void sleepBeforeRetry(long delayMillis) {
        try {
            sleeper.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("AI provider retry interrupted. provider={}", providerName);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }
    }

    @Override
    public String getModel() {
        return model;
    }

    private String toFailureReason(int status) {
        if (status == 401 || status == 403) {
            return "AUTHENTICATION_FAILED";
        }
        if (status == 429) {
            return "RATE_LIMITED";
        }
        if (status >= 500) {
            return "PROVIDER_SERVER_ERROR";
        }
        return "HTTP_ERROR";
    }

    private boolean hasTimeoutCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String extractProviderErrorMessage(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "UNAVAILABLE";
        }
        Matcher matcher = ERROR_MESSAGE_PATTERN.matcher(responseBody);
        String message = matcher.find() ? matcher.group(1) : "UNAVAILABLE";
        if (StringUtils.hasText(apiKey)) {
            message = message.replace(apiKey, "[REDACTED]");
        }
        message = EMAIL_PATTERN.matcher(message).replaceAll("[REDACTED]");
        message = PHONE_PATTERN.matcher(message).replaceAll("[REDACTED]");
        message = message.replaceAll("[\\r\\n\\t]+", " ");
        return message.length() > MAX_PROVIDER_ERROR_MESSAGE_LENGTH
                ? message.substring(0, MAX_PROVIDER_ERROR_MESSAGE_LENGTH)
                : message;
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private record RetryDelay(long millis, String source) {
    }
}
