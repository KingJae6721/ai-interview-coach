package com.aiinterview.resume.entity;

import com.aiinterview.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "resume_analyses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeAnalysis extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    private Resume resume;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> skills;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "work_experiences", columnDefinition = "jsonb", nullable = false)
    private List<String> workExperiences;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> projects;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> education;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> certifications;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> achievements;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> strengths;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> keywords;

    @Column(name = "ai_model", nullable = false, length = 100)
    private String aiModel;
    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    @Builder
    private ResumeAnalysis(Resume resume, String summary, List<String> skills, List<String> workExperiences,
                           List<String> projects, List<String> education, List<String> certifications,
                           List<String> achievements, List<String> strengths, List<String> keywords,
                           String aiModel, LocalDateTime analyzedAt) {
        this.resume = resume;
        this.summary = summary;
        this.skills = List.copyOf(skills);
        this.workExperiences = List.copyOf(workExperiences);
        this.projects = List.copyOf(projects);
        this.education = List.copyOf(education);
        this.certifications = List.copyOf(certifications);
        this.achievements = List.copyOf(achievements);
        this.strengths = List.copyOf(strengths);
        this.keywords = List.copyOf(keywords);
        this.aiModel = aiModel;
        this.analyzedAt = analyzedAt;
    }
}
