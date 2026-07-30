package com.aiinterview.interview.service;

import com.aiinterview.ai.service.OpenAiService;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateRequest;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
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
}
