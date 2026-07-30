package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    Optional<InterviewAnswer> findByInterviewQuestionId(Long interviewQuestionId);

    long countByInterviewQuestionInterviewId(Long interviewId);
}
