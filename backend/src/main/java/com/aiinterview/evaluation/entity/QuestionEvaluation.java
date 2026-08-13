package com.aiinterview.evaluation.entity;

import com.aiinterview.common.entity.BaseEntity;
import com.aiinterview.interview.entity.InterviewAnswer;
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
@Table(name = "question_evaluations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionEvaluation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id", nullable = false, unique = true)
    private InterviewAnswer answer;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String strengths;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "improvement_suggestion", nullable = false, columnDefinition = "TEXT")
    private String improvementSuggestion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reasoning;

    @Column(name = "ai_model", nullable = false, length = 50)
    private String aiModel;

    @Builder
    private QuestionEvaluation(InterviewAnswer answer, int score, String strengths, String weaknesses,
                               String improvementSuggestion, String reasoning, String aiModel) {
        this.answer = answer;
        this.score = score;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.improvementSuggestion = improvementSuggestion;
        this.reasoning = reasoning;
        this.aiModel = aiModel;
    }
}
