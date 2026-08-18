package com.aiinterview.interview.service;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.entity.InterviewQuestionType;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class InterviewFollowUpQuestionPersistenceService {

    private final InterviewQuestionRepository interviewQuestionRepository;

    @Transactional
    public InterviewQuestion save(Long parentQuestionId, String content) {
        InterviewQuestion parentQuestion = interviewQuestionRepository.findWithInterviewAndUserById(parentQuestionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_QUESTION_NOT_FOUND));

        return interviewQuestionRepository.saveAndFlush(InterviewQuestion.builder()
                .interview(parentQuestion.getInterview())
                .parentQuestion(parentQuestion)
                .questionOrder(interviewQuestionRepository.findMaxQuestionOrderByInterviewId(
                        parentQuestion.getInterview().getId()) + 1)
                .content(content)
                .type(InterviewQuestionType.FOLLOW_UP)
                .isAiGenerated(true)
                .build());
    }
}
