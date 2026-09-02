package com.aiinterview.evaluation.repository;

import com.aiinterview.evaluation.entity.QuestionEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionEvaluationRepository extends JpaRepository<QuestionEvaluation, Long> {

    boolean existsByAnswerId(Long answerId);

    @Query("""
            select evaluation
            from QuestionEvaluation evaluation
            join fetch evaluation.answer answer
            join answer.interviewQuestion question
            where question.interview.id = :interviewId
            """)
    List<QuestionEvaluation> findAllByInterviewIdWithAnswer(@Param("interviewId") Long interviewId);
}
