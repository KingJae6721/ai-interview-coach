package com.aiinterview.interview.entity;

import com.aiinterview.common.entity.BaseEntity;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_position_id")
    private JobPosition jobPosition;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    private Interview(User user, JobPosition jobPosition, String title, InterviewStatus status) {
        this.user = user;
        this.jobPosition = jobPosition;
        this.title = title;
        this.status = status == null ? InterviewStatus.READY : status;
    }

    public void start() {
        if (status == InterviewStatus.READY) {
            this.status = InterviewStatus.IN_PROGRESS;
        }
    }

    public void complete() {
        if (status != InterviewStatus.COMPLETED) {
            this.status = InterviewStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }
}
