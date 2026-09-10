package com.aiinterview.ai.provider;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AbstractOpenAiCompatibleProviderTest {

    private static final String RESPONSE = "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}";

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;
    private List<Long> delays;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder().baseUrl("https://provider.test");
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        delays = new ArrayList<>();
    }

    @Test
    void retries429UsingRetryAfterThenSucceeds() {
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "2")
                        .header("x-ratelimit-reset-tokens", "9s"));
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andRespond(withSuccess(RESPONSE, MediaType.APPLICATION_JSON));

        String response = provider(2, 100, 10_000).complete(request());

        assertThat(response).isEqualTo(RESPONSE);
        assertThat(delays).containsExactly(2_000L);
        server.verify();
    }

    @Test
    void usesTokenResetHeaderWhenRetryAfterIsMissing() {
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .header("x-ratelimit-reset-tokens", "1.25s"));
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andRespond(withSuccess(RESPONSE, MediaType.APPLICATION_JSON));

        provider(1, 100, 10_000).complete(request());

        assertThat(delays).containsExactly(1_250L);
        server.verify();
    }

    @Test
    void appliesBoundedExponentialBackoffWithoutProviderHeaders() {
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andRespond(withSuccess(RESPONSE, MediaType.APPLICATION_JSON));

        provider(2, 100, 150).complete(request());

        assertThat(delays).containsExactly(100L, 150L);
        server.verify();
    }

    @Test
    void failsAfterMaximum429Retries() {
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> provider(1, 100, 1_000).complete(request()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_REQUEST_FAILED));
        assertThat(delays).containsExactly(100L);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403})
    void doesNotRetryPermanentHttpErrors(int status) {
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andRespond(withStatus(HttpStatus.valueOf(status)));

        assertThatThrownBy(() -> provider(2, 100, 1_000).complete(request()))
                .isInstanceOf(BusinessException.class);
        assertThat(delays).isEmpty();
        server.verify();
    }

    @Test
    void sendsCompletionTokenLimit() {
        server.expect(once(), requestTo("https://provider.test/chat/completions"))
                .andExpect(content().json("{\"max_completion_tokens\":640}"))
                .andRespond(withSuccess(RESPONSE, MediaType.APPLICATION_JSON));

        provider(0, 100, 1_000).complete(request());

        server.verify();
    }

    private TestProvider provider(int maxRetries, long initialBackoffMillis, long maxBackoffMillis) {
        return new TestProvider(restClientBuilder.build(), maxRetries, initialBackoffMillis,
                maxBackoffMillis, delays::add);
    }

    private AiCompletionRequest request() {
        return new AiCompletionRequest("system", "user", null, 640);
    }

    private static final class TestProvider extends AbstractOpenAiCompatibleProvider {

        private TestProvider(RestClient restClient, int maxRetries, long initialBackoffMillis,
                             long maxBackoffMillis, Sleeper sleeper) {
            super("test", restClient, "test-key", "test-model", maxRetries,
                    initialBackoffMillis, maxBackoffMillis, sleeper);
        }
    }
}
