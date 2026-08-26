package com.aiinterview.interview.service;

import com.aiinterview.interview.dto.InterviewAnswerCreateRequest;
import com.aiinterview.interview.dto.InterviewAnswerCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateRequest;
import com.aiinterview.interview.dto.InterviewCompleteResponse;
import com.aiinterview.interview.dto.InterviewCancelResponse;
import com.aiinterview.interview.dto.InterviewStartResponse;
import com.aiinterview.interview.dto.InterviewStateResponse;
import com.aiinterview.interview.dto.InterviewQuestionResponse;
import com.aiinterview.interview.dto.InterviewProgressResponse;
import com.aiinterview.interview.dto.InterviewHistoryResponse;
import com.aiinterview.ai.dto.InterviewFollowUpQuestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InterviewService {

    InterviewCreateResponse createInterview(Long userId, InterviewCreateRequest request);

    Page<InterviewHistoryResponse> getInterviewHistory(Long userId, Pageable pageable);

    List<InterviewQuestionResponse> getInterviewQuestions(Long userId, Long interviewId);

    InterviewStartResponse startInterview(Long userId, Long interviewId);

    InterviewStateResponse getInterviewState(Long userId, Long interviewId);

    InterviewProgressResponse getInterviewProgress(Long userId, Long interviewId);

    InterviewAnswerCreateResponse submitAnswer(Long userId, Long questionId, InterviewAnswerCreateRequest request);

    InterviewFollowUpQuestionResponse generateFollowUpQuestion(Long userId, Long questionId);

    InterviewCompleteResponse completeInterview(Long userId, Long interviewId);

    InterviewCancelResponse cancelInterview(Long userId, Long interviewId);
}
