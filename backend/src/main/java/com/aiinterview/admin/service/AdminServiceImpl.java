package com.aiinterview.admin.service;

import com.aiinterview.admin.dto.AdminAuditLogResponse;
import com.aiinterview.admin.dto.AdminDashboardInterviewStatistics;
import com.aiinterview.admin.dto.AdminDashboardResponse;
import com.aiinterview.admin.dto.AdminInterviewDetailResponse;
import com.aiinterview.admin.dto.AdminInterviewListResponse;
import com.aiinterview.admin.dto.AdminInterviewQuestionResponse;
import com.aiinterview.admin.dto.AdminLatestInterviewResponse;
import com.aiinterview.admin.dto.AdminUserDetailResponse;
import com.aiinterview.admin.dto.AdminUserInterviewStatistics;
import com.aiinterview.admin.dto.AdminUserListResponse;
import com.aiinterview.admin.dto.AdminUserRoleUpdateResponse;
import com.aiinterview.admin.entity.AdminAuditAction;
import com.aiinterview.admin.entity.AdminAuditLog;
import com.aiinterview.admin.entity.AdminAuditTargetType;
import com.aiinterview.admin.repository.AdminAuditLogRepository;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.jobposting.repository.JobPostingAnalysisRepository;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.entity.UserRole;
import com.aiinterview.user.entity.UserStatus;
import com.aiinterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceImpl implements AdminService {

    private static final int RECENT_INTERVIEW_LIMIT = 10;

    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final JobPostingAnalysisRepository jobPostingAnalysisRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserListResponse> getUsers(
            String email, UserRole role, UserStatus status, Pageable pageable) {
        return userRepository.findAdminUsers(normalizeSearchTerm(email), role, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUser(Long userId) {
        User user = findUser(userId);
        AdminUserInterviewStatistics statistics =
                interviewRepository.findAdminUserInterviewStatistics(userId);
        AdminLatestInterviewResponse latestInterview = interviewRepository
                .findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(AdminLatestInterviewResponse::from)
                .orElse(null);

        return new AdminUserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                statistics,
                latestInterview);
    }

    @Override
    @Transactional
    public AdminUserRoleUpdateResponse updateUserRole(Long actorUserId, Long targetUserId, UserRole role) {
        User targetUser = findUser(targetUserId);
        UserRole beforeRole = targetUser.getRole();

        if (beforeRole == role) {
            return new AdminUserRoleUpdateResponse(targetUser.getId(), targetUser.getRole());
        }

        if (beforeRole == UserRole.ADMIN && role == UserRole.USER) {
            List<User> administrators = userRepository.findAllByRoleForUpdate(UserRole.ADMIN);
            if (administrators.size() <= 1) {
                throw new BusinessException(ErrorCode.LAST_ADMIN_DEMOTION_NOT_ALLOWED);
            }
            targetUser = administrators.stream()
                    .filter(administrator -> administrator.getId().equals(targetUserId))
                    .findFirst()
                    .orElse(targetUser);
        }

        targetUser.changeRole(role);
        adminAuditLogRepository.save(AdminAuditLog.builder()
                .actorUserId(actorUserId)
                .targetType(AdminAuditTargetType.USER)
                .targetId(targetUserId)
                .action(AdminAuditAction.USER_ROLE_CHANGED)
                .beforeValue(beforeRole.name())
                .afterValue(role.name())
                .build());

        return new AdminUserRoleUpdateResponse(targetUser.getId(), targetUser.getRole());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminInterviewListResponse> getInterviews(
            InterviewStatus status, String userEmail, Pageable pageable) {
        return interviewRepository.findAdminInterviews(status, normalizeSearchTerm(userEmail), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminInterviewDetailResponse getInterview(Long interviewId) {
        Interview interview = interviewRepository.findAdminDetailById(interviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND));

        Long jobPostingId = interview.getJobPosting() == null ? null : interview.getJobPosting().getId();
        String jobPostingTitle = interview.getJobPosting() == null ? null : interview.getJobPosting().getTitle();
        String jobPostingSummary = jobPostingId == null
                ? null
                : jobPostingAnalysisRepository.findSummaryByJobPostingId(jobPostingId).orElse(null);

        List<AdminInterviewQuestionResponse> questions = orderQuestions(
                interviewQuestionRepository.findAdminQuestionDetailsByInterviewId(interviewId));

        return new AdminInterviewDetailResponse(
                interview.getId(),
                interview.getTitle(),
                interview.getStatus(),
                interview.getUser().getId(),
                interview.getUser().getEmail(),
                jobPostingId,
                jobPostingTitle,
                jobPostingSummary,
                interview.getResume() != null,
                interview.getResume() == null ? null : interview.getResume().getId(),
                questions,
                interview.getCreatedAt(),
                interview.getStartedAt(),
                interview.getCompletedAt(),
                interview.getCancelledAt());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        AdminDashboardInterviewStatistics statistics =
                interviewRepository.findAdminDashboardStatistics(LocalDate.now().atStartOfDay());
        Pageable recentPage = PageRequest.of(
                0, RECENT_INTERVIEW_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AdminInterviewListResponse> recentInterviews = interviewRepository
                .findAdminInterviews(null, "", recentPage)
                .getContent();

        return new AdminDashboardResponse(
                userRepository.count(),
                statistics.totalInterviews(),
                statistics.readyInterviews(),
                statistics.inProgressInterviews(),
                statistics.completedInterviews(),
                statistics.cancelledInterviews(),
                statistics.todayCreatedInterviews(),
                recentInterviews);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminAuditLogResponse> getAuditLogs(
            AdminAuditAction action,
            Long actorUserId,
            AdminAuditTargetType targetType,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {
        LocalDateTime fromDateTime = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate == null ? null : toDate.plusDays(1).atStartOfDay();

        return adminAuditLogRepository.findAdminAuditLogs(
                        action, actorUserId, targetType, fromDateTime, toDateTime, pageable)
                .map(AdminAuditLogResponse::from);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizeSearchTerm(String searchTerm) {
        return StringUtils.hasText(searchTerm) ? searchTerm.trim() : "";
    }

    private List<AdminInterviewQuestionResponse> orderQuestions(
            List<AdminInterviewQuestionResponse> questions) {
        Map<Long, AdminInterviewQuestionResponse> followUpsByParentId = new HashMap<>();
        List<AdminInterviewQuestionResponse> roots = new ArrayList<>();

        for (AdminInterviewQuestionResponse question : questions) {
            if (question.parentQuestionId() == null) {
                roots.add(question);
            } else {
                followUpsByParentId.put(question.parentQuestionId(), question);
            }
        }

        roots.sort((left, right) -> left.questionOrder().compareTo(right.questionOrder()));
        List<AdminInterviewQuestionResponse> ordered = new ArrayList<>(questions.size());
        Set<Long> includedIds = new HashSet<>();
        for (AdminInterviewQuestionResponse root : roots) {
            ordered.add(root);
            includedIds.add(root.questionId());
            AdminInterviewQuestionResponse followUp = followUpsByParentId.get(root.questionId());
            if (followUp != null) {
                ordered.add(followUp);
                includedIds.add(followUp.questionId());
            }
        }
        questions.stream()
                .filter(question -> !includedIds.contains(question.questionId()))
                .forEach(ordered::add);
        return List.copyOf(ordered);
    }
}
