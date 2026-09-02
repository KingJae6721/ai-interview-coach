package com.aiinterview.feedback.repository;

import com.aiinterview.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByInterviewId(Long interviewId);

    Optional<Feedback> findByInterviewId(Long interviewId);

    @Query("select feedback from Feedback feedback join fetch feedback.interview "
            + "where feedback.interview.id in :interviewIds")
    List<Feedback> findAllByInterviewIdInWithInterview(@Param("interviewIds") Collection<Long> interviewIds);
}
