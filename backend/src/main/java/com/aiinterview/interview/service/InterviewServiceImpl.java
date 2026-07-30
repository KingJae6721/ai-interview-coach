package com.aiinterview.interview.service;

import com.aiinterview.ai.service.OpenAiService;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.dto.InterviewAnswerCreateRequest;
import com.aiinterview.interview.dto.InterviewAnswerCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateRequest;
import com.aiinterview.interview.dto.InterviewCompleteResponse;
import com.aiinterview.interview.dto.InterviewQuestionResponse;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewAnswer;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.interview.repository.InterviewAnswerRepository;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final UserRepository userRepository;
    private final OpenAiService openAiService;

    @Override
    @Transactional
    public InterviewCreateResponse createInterview(Long userId, InterviewCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Interview interview = interviewRepository.save(Interview.builder()
                .user(user)
                .title(request.getTitle())
                .status(InterviewStatus.READY)
                .build());

        List<String> generatedQuestions = openAiService.generateInterviewQuestions(interview.getTitle());
        List<InterviewQuestion> interviewQuestions = IntStream.range(0, generatedQuestions.size())
                .mapToObj(index -> InterviewQuestion.builder()
                        .interview(interview)
                        .questionOrder(index + 1)
                        .content(generatedQuestions.get(index))
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

    @Override
    @Transactional
    public List<InterviewQuestionResponse> getInterviewQuestions(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND));

        if (!interview.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        interview.start();

        return interviewQuestionRepository.findByInterviewIdOrderByQuestionOrderAsc(interviewId).stream()
                .map(interviewQuestion -> InterviewQuestionResponse.builder()
                        .questionOrder(interviewQuestion.getQuestionOrder())
                        .content(interviewQuestion.getContent())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public InterviewAnswerCreateResponse submitAnswer(Long userId, Long questionId,
                                                       InterviewAnswerCreateRequest request) {
        InterviewQuestion interviewQuestion = interviewQuestionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_QUESTION_NOT_FOUND));

        if (!interviewQuestion.getInterview().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        LocalDateTime answeredAt = LocalDateTime.now();
        return interviewAnswerRepository.findByInterviewQuestionId(questionId)
                .map(interviewAnswer -> {
                    interviewAnswer.updateAnswer(request.getAnswerContent(), answeredAt);
                    return toAnswerResponse(interviewAnswer, false);
                })
                .orElseGet(() -> {
                    InterviewAnswer interviewAnswer = interviewAnswerRepository.save(InterviewAnswer.builder()
                            .interviewQuestion(interviewQuestion)
                            .answerContent(request.getAnswerContent())
                            .answeredAt(answeredAt)
                            .build());
                    return toAnswerResponse(interviewAnswer, true);
                });
    }

    @Override
    @Transactional
    public InterviewCompleteResponse completeInterview(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND));

        if (!interview.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        long questionCount = interviewQuestionRepository.countByInterviewId(interviewId);
        long answerCount = interviewAnswerRepository.countByInterviewQuestionInterviewId(interviewId);
        if (questionCount == 0 || questionCount != answerCount) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_COMPLETABLE);
        }

        interview.complete();

        return InterviewCompleteResponse.builder()
                .interviewId(interview.getId())
                .status(interview.getStatus())
                .completedAt(interview.getCompletedAt())
                .build();
    }

    private InterviewAnswerCreateResponse toAnswerResponse(InterviewAnswer interviewAnswer, boolean created) {
        return InterviewAnswerCreateResponse.builder()
                .answerId(interviewAnswer.getId())
                .questionId(interviewAnswer.getInterviewQuestion().getId())
                .answerContent(interviewAnswer.getAnswerContent())
                .answeredAt(interviewAnswer.getAnsweredAt())
                .created(created)
                .build();
    }
}
