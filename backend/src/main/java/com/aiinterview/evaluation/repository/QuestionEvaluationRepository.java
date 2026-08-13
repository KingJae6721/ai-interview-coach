package com.aiinterview.evaluation.repository;

import com.aiinterview.evaluation.entity.QuestionEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionEvaluationRepository extends JpaRepository<QuestionEvaluation, Long> {

    boolean existsByAnswerId(Long answerId);
}
