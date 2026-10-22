package com.aiinterview.interview.repository;

import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.admin.dto.AdminInterviewQuestionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    List<InterviewQuestion> findAllByInterviewIdWithParentOrderByQuestionOrderAsc(
            @Param("interviewId") Long interviewId);

    long countByInterviewId(Long interviewId);

    boolean existsByParentQuestionId(Long parentQuestionId);

    Optional<InterviewQuestion> findByParentQuestionId(Long parentQuestionId);

    @Query("""
            select question
            from InterviewQuestion question
            join fetch question.interview interview
            join fetch interview.user
            left join fetch question.parentQuestion
            where question.id = :questionId
            """)
    Optional<InterviewQuestion> findWithInterviewAndUserById(@Param("questionId") Long questionId);

    @Query("select coalesce(max(question.questionOrder), 0) from InterviewQuestion question where question.interview.id = :interviewId")
    Integer findMaxQuestionOrderByInterviewId(@Param("interviewId") Long interviewId);

    @Query("""
            select new com.aiinterview.admin.dto.AdminInterviewQuestionResponse(
                question.id, parent.id, question.questionOrder, question.content,
                question.category, question.difficulty, question.type,
                case when answer.id is not null then true else false end,
                case when evaluation.id is not null then true else false end)
            from InterviewQuestion question
            left join question.parentQuestion parent
            left join InterviewAnswer answer on answer.interviewQuestion = question
            left join QuestionEvaluation evaluation on evaluation.answer = answer
            where question.interview.id = :interviewId
            order by question.questionOrder asc
            """)
    List<AdminInterviewQuestionResponse> findAdminQuestionDetailsByInterviewId(
            @Param("interviewId") Long interviewId);
}
