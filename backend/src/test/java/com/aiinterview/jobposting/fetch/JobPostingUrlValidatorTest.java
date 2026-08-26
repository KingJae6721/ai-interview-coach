package com.aiinterview.jobposting.fetch;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobPostingUrlValidatorTest {

    private final JobPostingUrlValidator validator = new JobPostingUrlValidator();

    @Test
    void validate_rejectsNonHttpUrl() {
        assertBlocked("file:///etc/passwd");
    }

    @Test
    void validate_rejectsLocalhostAndPrivateNetworks() {
        assertBlocked("http://localhost:8080/posting");
        assertBlocked("http://127.0.0.1/posting");
        assertBlocked("http://10.0.0.1/posting");
        assertBlocked("http://192.168.0.1/posting");
        assertBlocked("http://[::1]/posting");
    }

    @Test
    void validate_acceptsPublicHttpUrl() {
        assertThat(validator.validate("https://example.com/jobs/1").getHost()).isEqualTo("example.com");
    }

    private void assertBlocked(String url) {
        assertThatThrownBy(() -> validator.validate(url))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.JOB_POSTING_URL_NOT_ALLOWED);
    }
}
