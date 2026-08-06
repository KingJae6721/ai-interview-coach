package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    List<InterviewQuestion> findByInterviewIdOrderByQuestionOrderAsc(Long interviewId);

    @Query("""
            select question
            from InterviewQuestion question
            left join fetch question.parentQuestion
            where question.interview.id = :interviewId
            order by question.questionOrder asc
            """)
    List<InterviewQuestion> findAllByInterviewIdWithParentOrderByQuestionOrderAsc(Long interviewId);

    long countByInterviewId(Long interviewId);

    boolean existsByParentQuestionId(Long parentQuestionId);

    Optional<InterviewQuestion> findByParentQuestionId(Long parentQuestionId);

    @Query("""
            select question
            from InterviewQuestion question
            join fetch question.interview interview
            join fetch interview.user
            where question.id = :questionId
            """)
    Optional<InterviewQuestion> findWithInterviewAndUserById(Long questionId);

    @Query("select coalesce(max(question.questionOrder), 0) from InterviewQuestion question where question.interview.id = :interviewId")
    Integer findMaxQuestionOrderByInterviewId(Long interviewId);
}
