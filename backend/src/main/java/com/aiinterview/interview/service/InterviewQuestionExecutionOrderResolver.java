package com.aiinterview.interview.service;

import com.aiinterview.interview.entity.InterviewQuestion;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class InterviewQuestionExecutionOrderResolver {

    public List<InterviewQuestion> resolve(List<InterviewQuestion> questions) {
        Map<Long, InterviewQuestion> followUpByParentQuestionId = questions.stream()
                .filter(question -> question.getParentQuestion() != null)
                .collect(Collectors.toMap(
                        question -> question.getParentQuestion().getId(),
                        Function.identity(),
                        (first, ignored) -> first));

        return questions.stream()
                .filter(question -> question.getParentQuestion() == null)
                .sorted(Comparator.comparing(InterviewQuestion::getQuestionOrder))
                .flatMap(question -> java.util.stream.Stream.of(
                        question, followUpByParentQuestionId.get(question.getId())))
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
