package com.aiinterview.resume.entity;

import com.aiinterview.common.entity.BaseEntity;
import com.aiinterview.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "resumes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    @Column(name = "extracted_text", nullable = false, columnDefinition = "TEXT")
    private String extractedText;

    @Builder
    private Resume(User user, String originalFileName, long fileSize, String contentType,
                   String fileHash, String extractedText) {
        this.user = user;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.fileHash = fileHash;
        this.extractedText = extractedText;
    }
}
