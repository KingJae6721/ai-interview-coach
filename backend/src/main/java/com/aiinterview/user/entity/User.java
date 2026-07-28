package com.aiinterview.user.entity;

import com.aiinterview.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private User(String email, String password, String nickname, UserRole role,
                 AuthProvider authProvider, String providerId, UserStatus status) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : UserRole.USER;
        this.authProvider = authProvider != null ? authProvider : AuthProvider.LOCAL;
        this.providerId = providerId;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.deletedAt = null;
    }

    /**
     * 사용자의 닉네임을 변경합니다.
     */
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 사용자의 비밀번호를 변경합니다.
     */
    public void changePassword(String password) {
        this.password = password;
    }

    /**
     * 회원 탈퇴(Soft Delete) 처리를 진행합니다.
     */
    public void withdraw() {
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}
