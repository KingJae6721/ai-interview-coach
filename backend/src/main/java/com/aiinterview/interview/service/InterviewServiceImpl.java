package com.aiinterview.interview.service;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public InterviewCreateResponse createInterview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Interview interview = interviewRepository.save(Interview.builder()
                .user(user)
                .status(InterviewStatus.READY)
                .build());

        return InterviewCreateResponse.builder()
                .interviewId(interview.getId())
                .status(interview.getStatus())
                .build();
    }
}
