package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    @Query("select answer from InterviewAnswer answer "
            + "join fetch answer.interviewQuestion question "
            + "join fetch question.interview interview "
            + "join fetch interview.user "
            + "where answer.id = :answerId")
    Optional<InterviewAnswer> findWithQuestionInterviewAndUserById(Long answerId);

    Optional<InterviewAnswer> findByInterviewQuestionId(Long interviewQuestionId);

    long countByInterviewQuestionInterviewId(Long interviewId);

    @Query("""
            select answer
            from InterviewAnswer answer
            join fetch answer.interviewQuestion question
            where question.interview.id = :interviewId
            order by question.questionOrder asc
            """)
    java.util.List<InterviewAnswer> findAllByInterviewIdWithQuestion(Long interviewId);
}
