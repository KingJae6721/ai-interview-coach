package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    boolean existsByInterviewQuestionId(Long interviewQuestionId);
}
