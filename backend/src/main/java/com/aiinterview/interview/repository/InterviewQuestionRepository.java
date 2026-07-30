package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    List<InterviewQuestion> findByInterviewIdOrderByQuestionOrderAsc(Long interviewId);

    long countByInterviewId(Long interviewId);
}
