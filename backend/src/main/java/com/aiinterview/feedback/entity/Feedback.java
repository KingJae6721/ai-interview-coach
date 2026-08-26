package com.aiinterview.feedback.entity;

import com.aiinterview.common.entity.BaseEntity;
import com.aiinterview.interview.entity.Interview;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false, unique = true)
    private Interview interview;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(nullable = false)
    private boolean partial;

    @Column(name = "answered_count", nullable = false)
    private int answeredCount;

    @Column(name = "total_question_count", nullable = false)
    private int totalQuestionCount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String strengths;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "improvement_suggestions", nullable = false, columnDefinition = "TEXT")
    private String improvementSuggestions;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "ai_model", nullable = false, length = 50)
    private String aiModel;

    @Builder
    private Feedback(Interview interview, Integer overallScore, boolean partial, int answeredCount,
                     int totalQuestionCount, String strengths, String weaknesses,
                     String improvementSuggestions, String summary, String aiModel) {
        this.interview = interview;
        this.overallScore = overallScore;
        this.partial = partial;
        this.answeredCount = answeredCount;
        this.totalQuestionCount = totalQuestionCount;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.improvementSuggestions = improvementSuggestions;
        this.summary = summary;
        this.aiModel = aiModel;
    }
}
