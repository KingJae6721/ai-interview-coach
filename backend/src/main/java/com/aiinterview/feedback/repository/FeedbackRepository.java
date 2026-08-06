package com.aiinterview.feedback.repository;

import com.aiinterview.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByInterviewId(Long interviewId);

    Optional<Feedback> findByInterviewId(Long interviewId);
}
