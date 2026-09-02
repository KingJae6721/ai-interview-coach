package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    @Query("select answer from InterviewAnswer answer "
            + "join fetch answer.interviewQuestion question "
            + "join fetch question.interview interview "
            + "join fetch interview.user "
            + "where answer.id = :answerId")
    Optional<InterviewAnswer> findWithQuestionInterviewAndUserById(@Param("answerId") Long answerId);

    @Query("select answer from InterviewAnswer answer "
            + "join fetch answer.interviewQuestion question "
            + "join fetch question.interview interview "
            + "join fetch interview.user "
            + "where answer.id in :answerIds")
    List<InterviewAnswer> findAllWithQuestionInterviewAndUserByIdIn(
            @Param("answerIds") Collection<Long> answerIds);

    Optional<InterviewAnswer> findByInterviewQuestionId(Long interviewQuestionId);

    long countByInterviewQuestionInterviewId(Long interviewId);

    @Query("""
            select answer
            from InterviewAnswer answer
            join fetch answer.interviewQuestion question
            where question.interview.id = :interviewId
            order by question.questionOrder asc
            """)
    List<InterviewAnswer> findAllByInterviewIdWithQuestion(@Param("interviewId") Long interviewId);
}
