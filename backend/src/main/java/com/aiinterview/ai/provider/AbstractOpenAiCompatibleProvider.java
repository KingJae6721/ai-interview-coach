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

@Slf4j
abstract class AbstractOpenAiCompatibleProvider implements AiProvider {

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
            log.error("AI provider request failed. provider={}, reason={}, status={}", providerName,
                    toFailureReason(e.getStatusCode().value()), e.getStatusCode());
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
}
