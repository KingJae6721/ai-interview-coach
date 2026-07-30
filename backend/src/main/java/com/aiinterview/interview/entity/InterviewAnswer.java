package com.aiinterview.interview.entity;

import com.aiinterview.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "interview_answers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_interview_answer_question",
                columnNames = "question_id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestion interviewQuestion;

    @Column(name = "answer_content", nullable = false, columnDefinition = "TEXT")
    private String answerContent;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @Builder
    private InterviewAnswer(InterviewQuestion interviewQuestion, String answerContent, LocalDateTime answeredAt) {
        this.interviewQuestion = interviewQuestion;
        this.answerContent = answerContent;
        this.answeredAt = answeredAt;
    }
}
