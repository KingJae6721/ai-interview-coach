package com.aiinterview.ai.service;

import com.aiinterview.ai.dto.InterviewFeedbackRequest;
import com.aiinterview.ai.dto.InterviewFeedbackResult;
import com.aiinterview.ai.dto.JobPostingAnalysisResult;
import com.aiinterview.ai.dto.QuestionEvaluationRequest;
import com.aiinterview.ai.dto.QuestionEvaluationResult;
import com.aiinterview.ai.dto.ResumeAnalysisResult;
import com.aiinterview.ai.prompt.FeedbackPromptBuilder;
import com.aiinterview.ai.prompt.FollowUpQuestionPromptBuilder;
import com.aiinterview.ai.prompt.JobPostingAnalysisPromptBuilder;
import com.aiinterview.ai.prompt.QuestionEvaluationPolicy;
import com.aiinterview.ai.prompt.QuestionEvaluationPromptBuilder;
import com.aiinterview.ai.prompt.ResumeAnalysisPromptBuilder;
import com.aiinterview.ai.provider.AiCompletionRequest;
import com.aiinterview.ai.provider.AiProvider;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class OpenAiServiceImpl implements AiService {

    private static final int QUESTION_COUNT = 5;
    private static final int FEEDBACK_MAX_COMPLETION_TOKENS = 768;
    private static final int EVALUATION_MAX_COMPLETION_TOKENS = 640;
    private static final String INSUFFICIENT_STRENGTHS = "실질적인 강점을 확인할 수 없습니다.";
    private static final String INSUFFICIENT_WEAKNESSES = "질문에 대한 평가 가능한 답변이 제공되지 않았습니다.";
    private static final String INSUFFICIENT_IMPROVEMENT =
            "질문의 핵심 요구사항에 맞춰 구체적인 설명, 근거 또는 사례를 포함해 답변해 주세요.";
    private static final String INSUFFICIENT_REASONING =
            "답변이 질문에 대응하는 의미 있는 평가 정보를 포함하지 않아 평가할 수 없습니다.";

    private static final String QUESTION_GENERATION_PROMPT = """
            You are a technical interviewer. Generate exactly five interview questions from the provided interview context.
            Questions must be in Korean, concise, and appropriate for a software engineering interview.
            Follow the required question distribution in order and avoid duplicate or substantially similar questions.
            Return only a JSON array of five strings. Do not include markdown, explanations, or numbering.
            """;

    private final AiProvider aiProvider;
    private final ObjectMapper objectMapper;

    public OpenAiServiceImpl(ObjectMapper objectMapper, AiProvider aiProvider) {
        this.objectMapper = objectMapper;
        this.aiProvider = aiProvider;
    }

    @Override
    public List<String> generateInterviewQuestions(String questionGenerationPrompt) {
        try {
            String responseBody = aiProvider.complete(new AiCompletionRequest(
                    QUESTION_GENERATION_PROMPT, questionGenerationPrompt, null));

            return extractQuestions(responseBody);
        } catch (JacksonException e) {
            throw jsonDeserializationFailed(e);
        }
    }

    @Override
    public Optional<String> generateFollowUpQuestion(String answerContent) {
        try {
            String responseBody = aiProvider.complete(new AiCompletionRequest(
                    FollowUpQuestionPromptBuilder.buildSystemPrompt(),
                    FollowUpQuestionPromptBuilder.buildUserPrompt(answerContent), null));

            return extractFollowUpQuestion(responseBody);
        } catch (JacksonException e) {
            throw jsonDeserializationFailed(e);
        }
    }

    @Override
    public InterviewFeedbackResult generateInterviewFeedback(InterviewFeedbackRequest request) {
        try {
            String responseBody = aiProvider.complete(new AiCompletionRequest(
                    FeedbackPromptBuilder.buildSystemPrompt(), FeedbackPromptBuilder.buildUserPrompt(request),
                    feedbackResponseFormat(), FEEDBACK_MAX_COMPLETION_TOKENS));

            return extractFeedback(responseBody);
        } catch (JacksonException e) {
            throw jsonDeserializationFailed(e);
        }
    }

    @Override
    public QuestionEvaluationResult evaluateQuestionAnswer(QuestionEvaluationRequest request) {
        if (QuestionEvaluationPolicy.isClearlyInsufficient(
                request.getQuestionContent(), request.getAnswerContent())) {
            return insufficientQuestionEvaluation();
        }

        try {
            String responseBody = aiProvider.complete(new AiCompletionRequest(
                    QuestionEvaluationPromptBuilder.buildSystemPrompt(),
                    QuestionEvaluationPromptBuilder.buildUserPrompt(request),
                    questionEvaluationResponseFormat(request), EVALUATION_MAX_COMPLETION_TOKENS));

            return extractQuestionEvaluation(responseBody, request);
        } catch (JacksonException e) {
            throw jsonDeserializationFailed(e);
        }
    }

    @Override
    public JobPostingAnalysisResult analyzeJobPosting(String extractedContent) {
        try {
            String responseBody = aiProvider.complete(new AiCompletionRequest(
                    JobPostingAnalysisPromptBuilder.buildSystemPrompt(),
                    JobPostingAnalysisPromptBuilder.buildUserPrompt(extractedContent), jobPostingAnalysisResponseFormat()));

            return extractJobPostingAnalysis(responseBody);
        } catch (JacksonException e) {
            throw jsonDeserializationFailed(e);
        }
    }

    @Override
    public ResumeAnalysisResult analyzeResume(String extractedText) {
        try {
            String responseBody = aiProvider.complete(new AiCompletionRequest(
                    ResumeAnalysisPromptBuilder.buildSystemPrompt(),
                    ResumeAnalysisPromptBuilder.buildUserPrompt(extractedText), resumeAnalysisResponseFormat()));
            return extractResumeAnalysis(responseBody);
        } catch (JacksonException e) {
            throw jsonDeserializationFailed(e);
        }
    }

    private List<String> extractQuestions(String responseBody) throws JacksonException {
        JsonNode content = extractResponseContent(responseBody, "QUESTION_GENERATION");

        List<String> questions = objectMapper.readValue(removeJsonCodeFence(content.asText()), new TypeReference<>() {
        });

        if (questions.size() != QUESTION_COUNT || questions.stream().anyMatch(question -> !StringUtils.hasText(question))) {
            throw unexpectedResponseFormat();
        }

        return questions;
    }

    private Optional<String> extractFollowUpQuestion(String responseBody) throws JacksonException {
        JsonNode content = extractResponseContent(responseBody, "FOLLOW_UP_GENERATION");

        String followUpQuestion = content.asText().trim();
        if ("NO_FOLLOW_UP".equals(followUpQuestion)) {
            return Optional.empty();
        }
        if (!StringUtils.hasText(followUpQuestion)) {
            throw unexpectedResponseFormat();
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

    private Map<String, Object> questionEvaluationResponseFormat(QuestionEvaluationRequest request) {
        Map<String, Object> criterionProperties = new LinkedHashMap<>();
        QuestionEvaluationPolicy.criteria(request.getCategory()).forEach(criterion ->
                criterionProperties.put(criterion.key(),
                        Map.of("type", "integer", "minimum", 0, "maximum", 100)));

        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "question_evaluation",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "sufficient", Map.of("type", "boolean"),
                                        "criteria", Map.of(
                                                "type", "object",
                                                "properties", criterionProperties,
                                                "required", QuestionEvaluationPolicy.criteria(request.getCategory())
                                                        .stream()
                                                        .map(QuestionEvaluationPolicy.Criterion::key)
                                                        .toList(),
                                                "additionalProperties", false
                                        ),
                                        "strengths", Map.of("type", "string"),
                                        "weaknesses", Map.of("type", "string"),
                                        "improvementSuggestion", Map.of("type", "string"),
                                        "reasoning", Map.of("type", "string")
                                ),
                                "required", List.of(
                                        "sufficient", "criteria", "strengths", "weaknesses",
                                        "improvementSuggestion", "reasoning"
                                ),
                                "additionalProperties", false
                        )
                )
        );
    }

    private Map<String, Object> jobPostingAnalysisResponseFormat() {
        Map<String, Object> nullableString = Map.of("type", List.of("string", "null"));
        Map<String, Object> stringArray = Map.of("type", "array", "items", Map.of("type", "string"));

        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "job_posting_analysis",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "companyName", nullableString,
                                        "positionName", nullableString,
                                        "responsibilities", stringArray,
                                        "requiredQualifications", stringArray,
                                        "preferredQualifications", stringArray,
                                        "techStack", stringArray,
                                        "experienceRequirements", stringArray,
                                        "keywords", stringArray,
                                        "summary", nullableString
                                ),
                                "required", List.of(
                                        "companyName", "positionName", "responsibilities", "requiredQualifications",
                                        "preferredQualifications", "techStack", "experienceRequirements", "keywords", "summary"
                                ),
                                "additionalProperties", false
                        )
                )
        );
    }

    private Map<String, Object> resumeAnalysisResponseFormat() {
        Map<String, Object> nullableString = Map.of("type", List.of("string", "null"));
        Map<String, Object> stringArray = Map.of("type", "array", "items", Map.of("type", "string"));
        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "resume_analysis",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "summary", nullableString,
                                        "skills", stringArray,
                                        "workExperiences", stringArray,
                                        "projects", stringArray,
                                        "education", stringArray,
                                        "certifications", stringArray,
                                        "achievements", stringArray,
                                        "strengths", stringArray,
                                        "keywords", stringArray
                                ),
                                "required", List.of("summary", "skills", "workExperiences", "projects", "education",
                                        "certifications", "achievements", "strengths", "keywords"),
                                "additionalProperties", false
                        )
                )
        );
    }

    private InterviewFeedbackResult extractFeedback(String responseBody) throws JacksonException {
        JsonNode content = extractResponseContent(responseBody, "INTERVIEW_FEEDBACK");

        JsonNode feedback = objectMapper.readTree(content.asText());
        JsonNode overallScore = feedback.get("overallScore");
        if (overallScore == null || !overallScore.canConvertToInt()
                || overallScore.intValue() < 0 || overallScore.intValue() > 100) {
            throw unexpectedResponseFormat();
        }

        return InterviewFeedbackResult.builder()
                .overallScore(overallScore.intValue())
                .strengths(getRequiredText(feedback, "strengths"))
                .weaknesses(getRequiredText(feedback, "weaknesses"))
                .improvementSuggestions(getRequiredText(feedback, "improvementSuggestions"))
                .summary(getRequiredText(feedback, "summary"))
                .aiModel(aiProvider.getModel())
                .build();
    }

    private QuestionEvaluationResult extractQuestionEvaluation(String responseBody, QuestionEvaluationRequest request)
            throws JacksonException {
        JsonNode content = extractResponseContent(responseBody, "QUESTION_EVALUATION");

        JsonNode evaluation = objectMapper.readTree(removeJsonCodeFence(content.asText()));
        JsonNode sufficient = evaluation.get("sufficient");
        JsonNode criteria = evaluation.get("criteria");
        if (sufficient == null || !sufficient.isBoolean() || criteria == null || !criteria.isObject()) {
            throw unexpectedResponseFormat();
        }

        String strengths = getRequiredText(evaluation, "strengths");
        String weaknesses = getRequiredText(evaluation, "weaknesses");
        String improvementSuggestion = getRequiredText(evaluation, "improvementSuggestion");
        String reasoning = getRequiredText(evaluation, "reasoning");
        Map<String, Integer> criterionScores = extractCriterionScores(criteria, request);

        if (!sufficient.booleanValueOpt().orElse(false)) {
            return insufficientQuestionEvaluation();
        }

        int score = QuestionEvaluationPolicy.calculateScore(request.getCategory(), criterionScores);

        return QuestionEvaluationResult.builder()
                .score(score)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .improvementSuggestion(improvementSuggestion)
                .reasoning(reasoning)
                .aiModel(aiProvider.getModel())
                .build();
    }

    private Map<String, Integer> extractCriterionScores(JsonNode criteria, QuestionEvaluationRequest request) {
        Map<String, Integer> criterionScores = new LinkedHashMap<>();
        for (QuestionEvaluationPolicy.Criterion criterion
                : QuestionEvaluationPolicy.criteria(request.getCategory())) {
            JsonNode criterionScore = criteria.get(criterion.key());
            if (criterionScore == null || !criterionScore.canConvertToInt()
                    || criterionScore.intValue() < 0 || criterionScore.intValue() > 100) {
                throw unexpectedResponseFormat();
            }
            criterionScores.put(criterion.key(), criterionScore.intValue());
        }
        return criterionScores;
    }

    private QuestionEvaluationResult insufficientQuestionEvaluation() {
        return QuestionEvaluationResult.builder()
                .score(0)
                .strengths(INSUFFICIENT_STRENGTHS)
                .weaknesses(INSUFFICIENT_WEAKNESSES)
                .improvementSuggestion(INSUFFICIENT_IMPROVEMENT)
                .reasoning(INSUFFICIENT_REASONING)
                .aiModel(aiProvider.getModel())
                .build();
    }

    private JobPostingAnalysisResult extractJobPostingAnalysis(String responseBody) throws JacksonException {
        JsonNode content = extractResponseContent(responseBody, "JOB_POSTING_ANALYSIS");

        JsonNode analysis = objectMapper.readTree(removeJsonCodeFence(content.asText()));
        return JobPostingAnalysisResult.builder()
                .companyName(getNullableText(analysis, "companyName"))
                .positionName(getNullableText(analysis, "positionName"))
                .responsibilities(getRequiredTextList(analysis, "responsibilities"))
                .requiredQualifications(getRequiredTextList(analysis, "requiredQualifications"))
                .preferredQualifications(getRequiredTextList(analysis, "preferredQualifications"))
                .techStack(getRequiredTextList(analysis, "techStack"))
                .experienceRequirements(getRequiredTextList(analysis, "experienceRequirements"))
                .keywords(getRequiredTextList(analysis, "keywords"))
                .summary(getNullableText(analysis, "summary"))
                .aiModel(aiProvider.getModel())
                .build();
    }

    private ResumeAnalysisResult extractResumeAnalysis(String responseBody) throws JacksonException {
        JsonNode content = extractResponseContent(responseBody, "RESUME_ANALYSIS");
        JsonNode analysis = objectMapper.readTree(removeJsonCodeFence(content.asText()));
        return ResumeAnalysisResult.builder()
                .summary(getNullableText(analysis, "summary"))
                .skills(getRequiredTextList(analysis, "skills"))
                .workExperiences(getRequiredTextList(analysis, "workExperiences"))
                .projects(getRequiredTextList(analysis, "projects"))
                .education(getRequiredTextList(analysis, "education"))
                .certifications(getRequiredTextList(analysis, "certifications"))
                .achievements(getRequiredTextList(analysis, "achievements"))
                .strengths(getRequiredTextList(analysis, "strengths"))
                .keywords(getRequiredTextList(analysis, "keywords"))
                .aiModel(aiProvider.getModel())
                .build();
    }

    private String getRequiredText(JsonNode feedback, String fieldName) {
        JsonNode field = feedback.get(fieldName);
        if (field == null || !field.isTextual() || !StringUtils.hasText(field.asText())) {
            throw unexpectedResponseFormat();
        }
        return field.asText();
    }

    private String getNullableText(JsonNode json, String fieldName) {
        JsonNode field = json.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        if (!field.isTextual()) {
            throw unexpectedResponseFormat();
        }
        return StringUtils.hasText(field.asText()) ? field.asText() : null;
    }

    private List<String> getRequiredTextList(JsonNode json, String fieldName) {
        JsonNode field = json.get(fieldName);
        if (field == null || !field.isArray()) {
            throw unexpectedResponseFormat();
        }
        List<String> values = new java.util.ArrayList<>();
        for (JsonNode value : field) {
            if (!value.isTextual() || !StringUtils.hasText(value.asText())) {
                throw unexpectedResponseFormat();
            }
            values.add(value.asText());
        }
        return values;
    }

    private String removeJsonCodeFence(String content) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstLineEnd = trimmed.indexOf('\n');
        if (firstLineEnd < 0 || !trimmed.endsWith("```")) {
            return trimmed;
        }
        return trimmed.substring(firstLineEnd + 1, trimmed.length() - 3).trim();
    }

    private JsonNode extractResponseContent(String responseBody, String operation) throws JacksonException {
        JsonNode response = objectMapper.readTree(responseBody);
        JsonNode finishReasonNode = response.at("/choices/0/finish_reason");
        String finishReason = finishReasonNode.isTextual() ? finishReasonNode.asText() : "UNAVAILABLE";
        JsonNode content = response.at("/choices/0/message/content");

        if ("length".equals(finishReason)) {
            log.error("AI response processing failed. operation={}, reason=OUTPUT_TOKEN_LIMIT, finishReason=length",
                    operation);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }
        if (!content.isTextual() || !StringUtils.hasText(content.asText())) {
            log.error("AI response processing failed. operation={}, reason=CONTENT_MISSING, finishReason={}",
                    operation, finishReason);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED);
        }
        return content;
    }

    private BusinessException jsonDeserializationFailed(JacksonException exception) {
        log.error("AI response processing failed. reason=JSON_DESERIALIZATION_FAILED, errorType={}",
                exception.getClass().getSimpleName());
        return new BusinessException(ErrorCode.AI_REQUEST_FAILED);
    }

    private BusinessException unexpectedResponseFormat() {
        log.error("AI response processing failed. reason=UNEXPECTED_RESPONSE_FORMAT");
        return new BusinessException(ErrorCode.AI_REQUEST_FAILED);
    }
}
