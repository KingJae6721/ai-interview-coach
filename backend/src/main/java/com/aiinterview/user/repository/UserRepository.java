package com.aiinterview.user.repository;

import com.aiinterview.user.entity.User;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import com.aiinterview.admin.dto.AdminUserListResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Override
    Optional<User> findById(Long id);

    @Query(value = """
            select new com.aiinterview.admin.dto.AdminUserListResponse(
                user.id, user.email, user.role, user.status, user.createdAt, count(interview))
            from User user
            left join Interview interview on interview.user = user
            where (:email = '' or lower(user.email) like lower(concat('%', :email, '%')))
              and (:role is null or user.role = :role)
              and (:status is null or user.status = :status)
            group by user.id, user.email, user.role, user.status, user.createdAt
            """,
            countQuery = """
            select count(user)
            from User user
            where (:email = '' or lower(user.email) like lower(concat('%', :email, '%')))
              and (:role is null or user.role = :role)
              and (:status is null or user.status = :status)
            """)
    Page<AdminUserListResponse> findAdminUsers(
            @Param("email") String email,
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.role = :role order by user.id")
    List<User> findAllByRoleForUpdate(@Param("role") UserRole role);
}
