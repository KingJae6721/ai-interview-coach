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

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String providerName;

    protected AbstractOpenAiCompatibleProvider(String providerName, String baseUrl, String apiKey,
                                               String model, long timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.apiKey = apiKey;
        this.model = model;
        this.providerName = providerName;
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

        try {
            return restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
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
}
