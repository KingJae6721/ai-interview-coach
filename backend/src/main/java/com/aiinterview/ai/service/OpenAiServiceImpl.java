package com.aiinterview.ai.service;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiServiceImpl implements OpenAiService {

    private static final int QUESTION_COUNT = 5;

    private static final String QUESTION_GENERATION_PROMPT = """
            You are a technical interviewer. Generate exactly five interview questions for the interview title provided.
            Questions must be in Korean, concise, and appropriate for a software engineering interview.
            Return only a JSON array of five strings. Do not include markdown, explanations, or numbering.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String apiKey;

    public OpenAiServiceImpl(
            ObjectMapper objectMapper,
            @Value("${openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public List<String> generateInterviewQuestions(String interviewTitle) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }

        try {
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", model,
                            "messages", List.of(
                                    Map.of("role", "system", "content", QUESTION_GENERATION_PROMPT),
                                    Map.of("role", "user", "content", "Interview title: " + interviewTitle)
                            )
                    ))
                    .retrieve()
                    .body(String.class);

            return extractQuestions(responseBody);
        } catch (RestClientException | JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }
    }

    private List<String> extractQuestions(String responseBody) throws JsonProcessingException {
        JsonNode response = objectMapper.readTree(responseBody);
        JsonNode content = response.at("/choices/0/message/content");

        if (!content.isTextual()) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }

        List<String> questions = objectMapper.readValue(content.asText(), new TypeReference<>() {
        });

        if (questions.size() != QUESTION_COUNT || questions.stream().anyMatch(question -> !StringUtils.hasText(question))) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }

        return questions;
    }
}
