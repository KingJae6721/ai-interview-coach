package com.aiinterview.evaluation.repository;

import com.aiinterview.evaluation.entity.QuestionEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface QuestionEvaluationRepository extends JpaRepository<QuestionEvaluation, Long> {

    boolean existsByAnswerId(Long answerId);

    @Query("select evaluation.answer.id from QuestionEvaluation evaluation "
            + "where evaluation.answer.id in :answerIds")
    Set<Long> findEvaluatedAnswerIdsByAnswerIdIn(@Param("answerIds") Collection<Long> answerIds);

    @Query("""
            select evaluation
            from QuestionEvaluation evaluation
            join fetch evaluation.answer answer
            join answer.interviewQuestion question
            where question.interview.id = :interviewId
            """)
    List<QuestionEvaluation> findAllByInterviewIdWithAnswer(@Param("interviewId") Long interviewId);
}
