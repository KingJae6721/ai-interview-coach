package com.aiinterview.jobposting.fetch;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SecureJobPostingContentFetcher implements JobPostingContentFetcher {

    private static final int MAX_REDIRECTS = 3;
    private static final int MAX_RESPONSE_BYTES = 1_000_000;
    private static final int MAX_EXTRACTED_CONTENT_LENGTH = 30_000;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final JobPostingUrlValidator jobPostingUrlValidator;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Override
    public FetchedJobPostingContent fetch(String postingUrl) {
        URI currentUri = jobPostingUrlValidator.validate(postingUrl);

        for (int redirectCount = 0; redirectCount <= MAX_REDIRECTS; redirectCount++) {
            HttpResponse<InputStream> response = send(currentUri);
            if (isRedirect(response.statusCode())) {
                currentUri = resolveRedirect(currentUri, response);
                continue;
            }
            if (!HttpStatusCode.valueOf(response.statusCode()).is2xxSuccessful()) {
                throw new BusinessException(ErrorCode.JOB_POSTING_FETCH_FAILED);
            }
            validateContentType(response.headers());
            return extractContent(readLimited(response.body()));
        }

        throw new BusinessException(ErrorCode.JOB_POSTING_FETCH_FAILED);
    }

    private HttpResponse<InputStream> send(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "text/html,application/xhtml+xml")
                .header("User-Agent", "AI-Interview-Coach/1.0 JobPostingAnalyzer")
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.JOB_POSTING_FETCH_FAILED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.JOB_POSTING_FETCH_FAILED);
        }
    }

    private URI resolveRedirect(URI currentUri, HttpResponse<?> response) {
        String location = response.headers().firstValue("Location")
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSTING_FETCH_FAILED));
        return jobPostingUrlValidator.validate(currentUri.resolve(location).toString());
    }

    private boolean isRedirect(int statusCode) {
        return statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307 || statusCode == 308;
    }

    private void validateContentType(java.net.http.HttpHeaders headers) {
        String contentType = headers.firstValue("Content-Type").orElse(null);
        if (StringUtils.hasText(contentType)
                && !contentType.toLowerCase().contains("text/html")
                && !contentType.toLowerCase().contains("application/xhtml+xml")) {
            throw new BusinessException(ErrorCode.JOB_POSTING_CONTENT_NOT_FOUND);
        }
    }

    private String readLimited(InputStream inputStream) {
        try (InputStream stream = inputStream) {
            byte[] bytes = stream.readNBytes(MAX_RESPONSE_BYTES + 1);
            if (bytes.length > MAX_RESPONSE_BYTES) {
                throw new BusinessException(ErrorCode.JOB_POSTING_CONTENT_NOT_FOUND);
            }
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.JOB_POSTING_FETCH_FAILED);
        }
    }

    private FetchedJobPostingContent extractContent(String html) {
        Document document = Jsoup.parse(html);
        document.select("script, style, nav, footer, header, aside, noscript, svg, iframe").remove();
        Element contentRoot = document.selectFirst("main, [role=main], article");
        if (contentRoot == null) {
            contentRoot = document.body();
        }
        if (contentRoot == null) {
            throw new BusinessException(ErrorCode.JOB_POSTING_CONTENT_NOT_FOUND);
        }

        String content = contentRoot.text().trim();
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ErrorCode.JOB_POSTING_CONTENT_NOT_FOUND);
        }
        String limitedContent = content.length() > MAX_EXTRACTED_CONTENT_LENGTH
                ? content.substring(0, MAX_EXTRACTED_CONTENT_LENGTH)
                : content;
        String title = StringUtils.hasText(document.title()) ? document.title().trim() : null;
        return new FetchedJobPostingContent(title, limitedContent);
    }
}
