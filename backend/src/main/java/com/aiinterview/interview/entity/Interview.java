package com.aiinterview.interview.entity;

import com.aiinterview.common.entity.BaseEntity;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.jobposting.entity.JobPosting;
import com.aiinterview.resume.entity.Resume;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id")
    private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Builder
    private Interview(User user, JobPosition jobPosition, JobPosting jobPosting, Resume resume,
                      String title, InterviewStatus status) {
        this.user = user;
        this.jobPosition = jobPosition;
        this.jobPosting = jobPosting;
        this.resume = resume;
        this.title = title;
        this.status = status == null ? InterviewStatus.READY : status;
    }

    public void start() {
        this.status = InterviewStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        if (status != InterviewStatus.COMPLETED) {
            this.status = InterviewStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }

    public void cancel() {
        this.status = InterviewStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
}
