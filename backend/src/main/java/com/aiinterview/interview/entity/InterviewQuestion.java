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

@Entity
@Table(
        name = "interview_questions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_interview_question_order",
                columnNames = {"interview_id", "question_order"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isAiGenerated;

    @Builder
    private InterviewQuestion(Interview interview, Integer questionOrder, String content, Boolean isAiGenerated) {
        this.interview = interview;
        this.questionOrder = questionOrder;
        this.content = content;
        this.isAiGenerated = isAiGenerated != null && isAiGenerated;
    }
}
