package com.aiinterview.interview.service;

import com.aiinterview.ai.dto.InterviewQuestionDistribution;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.entity.InterviewQuestionType;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.jobposition.repository.JobPositionRepository;
import com.aiinterview.jobposting.entity.JobPosting;
import com.aiinterview.jobposting.repository.JobPostingRepository;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
class InterviewCreationPersistenceService {

    private final UserRepository userRepository;
    private final JobPositionRepository jobPositionRepository;
    private final JobPostingRepository jobPostingRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;

    @Transactional
    public InterviewCreateResponse save(Long userId, Long jobPositionId, Long jobPostingId, String title,
                                        List<String> generatedQuestions,
                                        List<InterviewQuestionDistribution> distributions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        JobPosition jobPosition = jobPositionRepository.findById(jobPositionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSITION_NOT_FOUND));
        JobPosting jobPosting = findJobPosting(jobPostingId, jobPositionId);

        Interview interview = interviewRepository.save(Interview.builder()
                .user(user)
                .jobPosition(jobPosition)
                .jobPosting(jobPosting)
                .title(title)
                .status(InterviewStatus.READY)
                .build());

        List<InterviewQuestion> interviewQuestions = IntStream.range(0, generatedQuestions.size())
                .mapToObj(index -> InterviewQuestion.builder()
                        .interview(interview)
                        .questionOrder(index + 1)
                        .content(generatedQuestions.get(index))
                        .category(distributions.get(index).category())
                        .difficulty(distributions.get(index).difficulty())
                        .type(InterviewQuestionType.NORMAL)
                        .isAiGenerated(true)
                        .build())
                .toList();
        interviewQuestionRepository.saveAll(interviewQuestions);

        return InterviewCreateResponse.builder()
                .interviewId(interview.getId())
                .title(interview.getTitle())
                .status(interview.getStatus())
                .questionCount(interviewQuestions.size())
                .build();
    }

    private JobPosting findJobPosting(Long jobPostingId, Long jobPositionId) {
        if (jobPostingId == null) {
            return null;
        }
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSTING_NOT_FOUND));
        if (!jobPosting.getJobPosition().getId().equals(jobPositionId)) {
            throw new BusinessException(ErrorCode.JOB_POSTING_POSITION_MISMATCH);
        }
        return jobPosting;
    }
}
