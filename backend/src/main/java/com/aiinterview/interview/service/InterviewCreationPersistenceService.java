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
import com.aiinterview.jobposting.entity.JobPosting;
import com.aiinterview.jobposting.repository.JobPostingRepository;
import com.aiinterview.resume.entity.Resume;
import com.aiinterview.resume.repository.ResumeAnalysisRepository;
import com.aiinterview.resume.repository.ResumeRepository;
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
    private final JobPostingRepository jobPostingRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;

    @Transactional
    public InterviewCreateResponse save(Long userId, Long jobPostingId, Long resumeId, String title,
                                        List<String> generatedQuestions,
                                        List<InterviewQuestionDistribution> distributions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSTING_NOT_FOUND));
        JobPosition jobPosition = jobPosting.getJobPosition();
        Resume resume = findResume(resumeId, userId);

        Interview interview = interviewRepository.save(Interview.builder()
                .user(user)
                .jobPosition(jobPosition)
                .jobPosting(jobPosting)
                .resume(resume)
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

    private Resume findResume(Long resumeId, Long userId) {
        if (resumeId == null) {
            return null;
        }
        Resume resume = resumeRepository.findWithUserById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
        if (!resume.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.RESUME_ACCESS_DENIED);
        }
        if (!resumeAnalysisRepository.existsByResumeId(resumeId)) {
            throw new BusinessException(ErrorCode.RESUME_NOT_ANALYZED);
        }
        return resume;
    }
}
