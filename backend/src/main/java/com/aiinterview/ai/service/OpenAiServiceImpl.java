package com.aiinterview.ai.service;

import com.aiinterview.ai.dto.InterviewFeedbackRequest;
import com.aiinterview.ai.dto.InterviewFeedbackResult;
import com.aiinterview.ai.prompt.FeedbackPromptBuilder;
import com.aiinterview.ai.prompt.FollowUpQuestionPromptBuilder;
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
import java.util.Optional;

@Service
public class OpenAiServiceImpl implements OpenAiService {

    private static final int QUESTION_COUNT = 5;

    private static final String QUESTION_GENERATION_PROMPT = """
            You are a technical interviewer. Generate exactly five interview questions from the provided interview context.
            Questions must be in Korean, concise, and appropriate for a software engineering interview.
            Follow the required question distribution in order and avoid duplicate or substantially similar questions.
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
    public List<String> generateInterviewQuestions(String questionGenerationPrompt) {
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
                                    Map.of("role", "user", "content", questionGenerationPrompt)
                            )
                    ))
                    .retrieve()
                    .body(String.class);

            return extractQuestions(responseBody);
        } catch (RestClientException | JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }
    }

    @Override
    public Optional<String> generateFollowUpQuestion(String answerContent) {
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
                                    Map.of("role", "system", "content", FollowUpQuestionPromptBuilder.buildSystemPrompt()),
                                    Map.of("role", "user", "content", FollowUpQuestionPromptBuilder.buildUserPrompt(answerContent))
                            )
                    ))
                    .retrieve()
                    .body(String.class);

            return extractFollowUpQuestion(responseBody);
        } catch (RestClientException | JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }
    }

    @Override
    public InterviewFeedbackResult generateInterviewFeedback(InterviewFeedbackRequest request) {
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
                                    Map.of("role", "system", "content", FeedbackPromptBuilder.buildSystemPrompt()),
                                    Map.of("role", "user", "content", FeedbackPromptBuilder.buildUserPrompt(request))
                            ),
                            "response_format", feedbackResponseFormat()
                    ))
                    .retrieve()
                    .body(String.class);

            return extractFeedback(responseBody);
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

    private Optional<String> extractFollowUpQuestion(String responseBody) throws JsonProcessingException {
        JsonNode response = objectMapper.readTree(responseBody);
        JsonNode content = response.at("/choices/0/message/content");

        if (!content.isTextual()) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }

        String followUpQuestion = content.asText().trim();
        if ("NO_FOLLOW_UP".equals(followUpQuestion)) {
            return Optional.empty();
        }
        if (!StringUtils.hasText(followUpQuestion)) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }
        return Optional.of(followUpQuestion);
    }

    private Map<String, Object> feedbackResponseFormat() {
        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "interview_feedback",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "overallScore", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                                        "strengths", Map.of("type", "string"),
                                        "weaknesses", Map.of("type", "string"),
                                        "improvementSuggestions", Map.of("type", "string"),
                                        "summary", Map.of("type", "string")
                                ),
                                "required", List.of(
                                        "overallScore", "strengths", "weaknesses", "improvementSuggestions", "summary"
                                ),
                                "additionalProperties", false
                        )
                )
        );
    }

    private InterviewFeedbackResult extractFeedback(String responseBody) throws JsonProcessingException {
        JsonNode response = objectMapper.readTree(responseBody);
        JsonNode content = response.at("/choices/0/message/content");

        if (!content.isTextual()) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }

        JsonNode feedback = objectMapper.readTree(content.asText());
        JsonNode overallScore = feedback.get("overallScore");
        if (overallScore == null || !overallScore.canConvertToInt()
                || overallScore.intValue() < 0 || overallScore.intValue() > 100) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }

        return InterviewFeedbackResult.builder()
                .overallScore(overallScore.intValue())
                .strengths(getRequiredText(feedback, "strengths"))
                .weaknesses(getRequiredText(feedback, "weaknesses"))
                .improvementSuggestions(getRequiredText(feedback, "improvementSuggestions"))
                .summary(getRequiredText(feedback, "summary"))
                .aiModel(model)
                .build();
    }

    private String getRequiredText(JsonNode feedback, String fieldName) {
        JsonNode field = feedback.get(fieldName);
        if (field == null || !field.isTextual() || !StringUtils.hasText(field.asText())) {
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }
        return field.asText();
    }
}
