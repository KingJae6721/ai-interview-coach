package com.aiinterview.ai.prompt;

import com.aiinterview.interview.entity.InterviewQuestionCategory;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class QuestionEvaluationPolicy {

    private static final Set<String> CLEARLY_INSUFFICIENT_ANSWERS = Set.of(
            "네", "아니요", "모름", "몰라요", "모르겠어요", "모르겠습니다", "잘모르겠습니다",
            "없음", "없습니다", "생각안납니다", "기억안납니다", "n/a", "na", "idk",
            "dontknow", "idontknow", "noidea"
    );
    private static final List<String> NUMERIC_ANSWER_QUESTION_CUES = List.of(
            "몇", "숫자", "번호", "코드", "개수", "횟수", "점수", "연도",
            "how many", "number", "code", "count", "score", "year"
    );

    private static final Map<InterviewQuestionCategory, List<Criterion>> RUBRICS = Map.of(
            InterviewQuestionCategory.CS, List.of(
                    new Criterion("requirementFulfillment", "질문이 요구한 핵심 내용 충족", 30),
                    new Criterion("technicalAccuracy", "기술적 정확성과 핵심 개념", 30),
                    new Criterion("explanationAndEvidence", "설명, 근거, 인과관계", 25),
                    new Criterion("clarity", "명확하고 논리적인 전달", 15)
            ),
            InterviewQuestionCategory.TECH_STACK, List.of(
                    new Criterion("requirementFulfillment", "질문이 요구한 핵심 내용 충족", 30),
                    new Criterion("technicalAccuracy", "기술적 정확성과 핵심 개념", 30),
                    new Criterion("explanationAndEvidence", "설명, 근거, 인과관계", 25),
                    new Criterion("clarity", "명확하고 논리적인 전달", 15)
            ),
            InterviewQuestionCategory.EXPERIENCE, List.of(
                    new Criterion("requirementFulfillment", "질문의 세부 요구사항 충족", 20),
                    new Criterion("situationSpecificity", "상황과 문제의 구체성", 15),
                    new Criterion("candidateAction", "지원자 본인의 행동과 기여", 25),
                    new Criterion("technicalJudgment", "판단 과정과 근거", 20),
                    new Criterion("resultAndReflection", "결과, 영향, 회고", 20)
            ),
            InterviewQuestionCategory.SITUATION, List.of(
                    new Criterion("requirementFulfillment", "질문의 세부 요구사항 충족", 20),
                    new Criterion("situationUnderstanding", "상황과 제약조건 이해", 20),
                    new Criterion("proposedAction", "구체적이고 실행 가능한 행동", 25),
                    new Criterion("reasoning", "선택의 논리와 근거", 25),
                    new Criterion("outcomeConsideration", "결과와 위험에 대한 고려", 10)
            ),
            InterviewQuestionCategory.COMPANY_FIT, List.of(
                    new Criterion("requirementFulfillment", "질문이 요구한 핵심 내용 충족", 30),
                    new Criterion("specificity", "답변의 구체성", 20),
                    new Criterion("companyRoleConnection", "회사 또는 직무와의 연결", 30),
                    new Criterion("reasoning", "논리성과 근거", 20)
            )
    );

    private QuestionEvaluationPolicy() {
    }

    public static List<Criterion> criteria(InterviewQuestionCategory category) {
        return RUBRICS.get(category);
    }

    public static int calculateScore(InterviewQuestionCategory category, Map<String, Integer> criterionScores) {
        List<Criterion> criteria = criteria(category);
        int weightedScore = criteria.stream()
                .mapToInt(criterion -> criterionScores.get(criterion.key()) * criterion.weight())
                .sum();
        int totalWeight = criteria.stream().mapToInt(Criterion::weight).sum();
        return Math.round((float) weightedScore / totalWeight);
    }

    public static boolean isClearlyInsufficient(String questionContent, String answerContent) {
        if (answerContent == null || answerContent.isBlank()) {
            return true;
        }

        String normalized = Normalizer.normalize(answerContent, Normalizer.Form.NFKC)
                .trim()
                .toLowerCase(Locale.ROOT);
        if (normalized.codePoints().noneMatch(Character::isLetter)) {
            boolean containsNumber = normalized.codePoints().anyMatch(Character::isDigit);
            return !containsNumber || !asksForNumericAnswer(questionContent);
        }

        String comparable = normalized.replaceAll("[\\p{P}\\p{S}\\s]+", "");
        return CLEARLY_INSUFFICIENT_ANSWERS.contains(comparable);
    }

    private static boolean asksForNumericAnswer(String questionContent) {
        if (questionContent == null) {
            return false;
        }
        String normalizedQuestion = Normalizer.normalize(questionContent, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
        return NUMERIC_ANSWER_QUESTION_CUES.stream().anyMatch(normalizedQuestion::contains);
    }

    public record Criterion(String key, String description, int weight) {
    }
}
