package com.aiinterview.interview.service;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.dto.InterviewAnswerCreateRequest;
import com.aiinterview.interview.dto.InterviewAnswerCreateResponse;
import com.aiinterview.interview.entity.InterviewAnswer;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.repository.InterviewAnswerRepository;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InterviewAnswerServiceImpl implements InterviewAnswerService {

    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;

    @Override
    @Transactional
    public InterviewAnswerCreateResponse createAnswer(Long userId, Long questionId, InterviewAnswerCreateRequest request) {
        InterviewQuestion interviewQuestion = interviewQuestionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_QUESTION_NOT_FOUND));

        if (!interviewQuestion.getInterview().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (interviewAnswerRepository.existsByInterviewQuestionId(questionId)) {
            throw new BusinessException(ErrorCode.INTERVIEW_ANSWER_ALREADY_EXISTS);
        }

        InterviewAnswer interviewAnswer = interviewAnswerRepository.save(InterviewAnswer.builder()
                .interviewQuestion(interviewQuestion)
                .answerContent(request.getAnswerContent())
                .answeredAt(LocalDateTime.now())
                .build());

        return InterviewAnswerCreateResponse.builder()
                .answerId(interviewAnswer.getId())
                .questionId(interviewQuestion.getId())
                .answerContent(interviewAnswer.getAnswerContent())
                .answeredAt(interviewAnswer.getAnsweredAt())
                .build();
    }
}
