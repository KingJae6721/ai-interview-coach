package com.aiinterview.ai.prompt;

import com.aiinterview.interview.entity.InterviewQuestionCategory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionEvaluationPolicyTest {

    @Test
    void clearlyInsufficientGate_normalizesKnownNonAnswersWithoutUsingLengthAlone() {
        String experienceQuestion = "어떤 기술적 도전을 어떻게 해결했나요?";
        assertThat(QuestionEvaluationPolicy.isClearlyInsufficient(experienceQuestion, "１")).isTrue();
        assertThat(QuestionEvaluationPolicy.isClearlyInsufficient(experienceQuestion, "잘   모르겠습니다.")).isTrue();
        assertThat(QuestionEvaluationPolicy.isClearlyInsufficient(experienceQuestion, "네")).isTrue();
        assertThat(QuestionEvaluationPolicy.isClearlyInsufficient(experienceQuestion, "원자성")).isFalse();
        assertThat(QuestionEvaluationPolicy.isClearlyInsufficient(experienceQuestion, "O(1)")).isFalse();
        assertThat(QuestionEvaluationPolicy.isClearlyInsufficient("Not Found 상태 코드는?", "404")).isFalse();
    }

    @Test
    void categoryRubrics_useOnlyExistingQuestionCategories() {
        assertThat(QuestionEvaluationPolicy.criteria(InterviewQuestionCategory.CS))
                .extracting(QuestionEvaluationPolicy.Criterion::key)
                .containsExactly("requirementFulfillment", "technicalAccuracy", "explanationAndEvidence", "clarity");
        assertThat(QuestionEvaluationPolicy.criteria(InterviewQuestionCategory.TECH_STACK))
                .extracting(QuestionEvaluationPolicy.Criterion::key)
                .contains("technicalAccuracy");
        assertThat(QuestionEvaluationPolicy.criteria(InterviewQuestionCategory.EXPERIENCE))
                .extracting(QuestionEvaluationPolicy.Criterion::key)
                .contains("candidateAction", "resultAndReflection");
        assertThat(QuestionEvaluationPolicy.criteria(InterviewQuestionCategory.SITUATION))
                .extracting(QuestionEvaluationPolicy.Criterion::key)
                .contains("proposedAction", "reasoning");
        assertThat(QuestionEvaluationPolicy.criteria(InterviewQuestionCategory.COMPANY_FIT))
                .extracting(QuestionEvaluationPolicy.Criterion::key)
                .contains("companyRoleConnection");
    }

    @Test
    void calculateScore_usesCategoryWeightsInsteadOfAiOverallGuess() {
        int score = QuestionEvaluationPolicy.calculateScore(InterviewQuestionCategory.EXPERIENCE, Map.of(
                "requirementFulfillment", 50,
                "situationSpecificity", 70,
                "candidateAction", 0,
                "technicalJudgment", 10,
                "resultAndReflection", 0
        ));

        assertThat(score).isEqualTo(23);
    }

    @Test
    void missingLegacyCategory_usesFallbackRubricInsteadOfThrowingNullPointerException() {
        assertThat(QuestionEvaluationPolicy.criteria(null))
                .extracting(QuestionEvaluationPolicy.Criterion::key)
                .containsExactly(
                        "requirementFulfillment", "relevanceAndAccuracy", "explanationAndEvidence", "clarity");
        assertThat(QuestionEvaluationPolicy.calculateScore(null, Map.of(
                "requirementFulfillment", 70,
                "relevanceAndAccuracy", 70,
                "explanationAndEvidence", 70,
                "clarity", 70
        ))).isEqualTo(70);
    }
}
