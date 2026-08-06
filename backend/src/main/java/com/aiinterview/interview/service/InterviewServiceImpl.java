package com.aiinterview.interview.service;

import com.aiinterview.ai.service.OpenAiService;
import com.aiinterview.ai.dto.InterviewQuestionDistribution;
import com.aiinterview.ai.dto.InterviewFollowUpQuestionResponse;
import com.aiinterview.ai.prompt.InterviewQuestionDistributionPolicy;
import com.aiinterview.ai.prompt.InterviewQuestionPromptBuilder;
import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.exception.BusinessException;
import com.aiinterview.interview.dto.InterviewAnswerCreateRequest;
import com.aiinterview.interview.dto.InterviewAnswerCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateResponse;
import com.aiinterview.interview.dto.InterviewCreateRequest;
import com.aiinterview.interview.dto.InterviewCompleteResponse;
import com.aiinterview.interview.dto.InterviewCompleteUnavailableResponse;
import com.aiinterview.interview.dto.InterviewQuestionResponse;
import com.aiinterview.interview.dto.InterviewProgressQuestionResponse;
import com.aiinterview.interview.dto.InterviewProgressResponse;
import com.aiinterview.interview.dto.InterviewHistoryResponse;
import com.aiinterview.feedback.entity.Feedback;
import com.aiinterview.feedback.repository.FeedbackRepository;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewAnswer;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.entity.InterviewQuestionType;
import com.aiinterview.interview.entity.InterviewStatus;
import com.aiinterview.interview.repository.InterviewAnswerRepository;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import com.aiinterview.interview.repository.InterviewRepository;
import com.aiinterview.jobposition.entity.JobPosition;
import com.aiinterview.jobposition.repository.JobPositionRepository;
import com.aiinterview.user.entity.User;
import com.aiinterview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.stream.IntStream;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final JobPositionRepository jobPositionRepository;
    private final OpenAiService openAiService;

    @Override
    @Transactional
    public InterviewCreateResponse createInterview(Long userId, InterviewCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        JobPosition jobPosition = jobPositionRepository.findWithCompanyById(request.getJobPositionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSITION_NOT_FOUND));

        Interview interview = interviewRepository.save(Interview.builder()
                .user(user)
                .jobPosition(jobPosition)
                .title(request.getTitle())
                .status(InterviewStatus.READY)
                .build());

        List<InterviewQuestionDistribution> distributions = InterviewQuestionDistributionPolicy.create(
                interview, InterviewQuestionDistributionPolicy.DEFAULT_QUESTION_COUNT);
        List<String> generatedQuestions = openAiService.generateInterviewQuestions(
                InterviewQuestionPromptBuilder.buildUserPrompt(interview, distributions));
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

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewHistoryResponse> getInterviewHistory(Long userId, Pageable pageable) {
        Pageable createdAtDescending = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Interview> interviews = interviewRepository.findByUserId(userId, createdAtDescending);

        Map<Long, Feedback> feedbackByInterviewId = interviews.isEmpty()
                ? Map.of()
                : feedbackRepository.findAllByInterviewIdInWithInterview(interviews.getContent().stream()
                                .map(Interview::getId)
                                .toList())
                        .stream()
                        .collect(Collectors.toMap(feedback -> feedback.getInterview().getId(), Function.identity()));

        return interviews.map(interview -> toHistoryResponse(interview, feedbackByInterviewId.get(interview.getId())));
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
    @Transactional(readOnly = true)
    public InterviewProgressResponse getInterviewProgress(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findWithUserById(interviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND));

        if (!interview.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (interview.getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_IN_PROGRESS);
        }

        List<InterviewQuestion> questions = interviewQuestionRepository
                .findAllByInterviewIdWithParentOrderByQuestionOrderAsc(interviewId);
        Map<Long, InterviewAnswer> answersByQuestionId = interviewAnswerRepository
                .findAllByInterviewIdWithQuestion(interviewId).stream()
                .collect(Collectors.toMap(answer -> answer.getInterviewQuestion().getId(), Function.identity()));

        List<InterviewProgressQuestionResponse> questionResponses = questions.stream()
                .map(question -> toProgressQuestionResponse(question, answersByQuestionId.get(question.getId())))
                .toList();
        Long nextQuestionId = findNextQuestionId(questions, answersByQuestionId);
        boolean allAnswered = !questions.isEmpty() && questions.size() == answersByQuestionId.size();

        return InterviewProgressResponse.builder()
                .interviewId(interview.getId())
                .status(interview.getStatus())
                .questions(questionResponses)
                .nextQuestionId(nextQuestionId)
                .allAnswered(allAnswered)
                .build();
    }

    @Override
    @Transactional
    public InterviewAnswerCreateResponse submitAnswer(Long userId, Long questionId,
                                                       InterviewAnswerCreateRequest request) {
        InterviewQuestion interviewQuestion = interviewQuestionRepository.findWithInterviewAndUserById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_QUESTION_NOT_FOUND));

        if (!interviewQuestion.getInterview().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (interviewQuestion.getInterview().getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_IN_PROGRESS);
        }

        Long interviewId = interviewQuestion.getInterview().getId();
        List<InterviewQuestion> questions = interviewQuestionRepository.findByInterviewIdOrderByQuestionOrderAsc(interviewId);
        Map<Long, InterviewAnswer> answersByQuestionId = findAnswersByQuestionId(interviewId);

        if (answersByQuestionId.containsKey(questionId)) {
            throw new BusinessException(ErrorCode.INTERVIEW_ANSWER_ALREADY_EXISTS);
        }

        Long nextQuestionId = findNextQuestionId(questions, answersByQuestionId);
        if (!questionId.equals(nextQuestionId)) {
            throw new BusinessException(ErrorCode.ANSWER_ORDER_INVALID);
        }

        try {
            InterviewAnswer interviewAnswer = interviewAnswerRepository.saveAndFlush(InterviewAnswer.builder()
                    .interviewQuestion(interviewQuestion)
                    .answerContent(request.getAnswerContent())
                    .answeredAt(LocalDateTime.now())
                    .build());
            return toAnswerResponse(interviewAnswer, true);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.INTERVIEW_ANSWER_ALREADY_EXISTS);
        }
    }

    @Override
    @Transactional
    public InterviewFollowUpQuestionResponse generateFollowUpQuestion(Long userId, Long questionId) {
        InterviewQuestion parentQuestion = interviewQuestionRepository.findWithInterviewAndUserById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_QUESTION_NOT_FOUND));

        if (!parentQuestion.getInterview().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return interviewQuestionRepository.findByParentQuestionId(questionId)
                .map(followUpQuestion -> toFollowUpResponse(parentQuestion, followUpQuestion, false))
                .orElseGet(() -> createFollowUpQuestion(parentQuestion));
    }

    @Override
    @Transactional
    public InterviewCompleteResponse completeInterview(Long userId, Long interviewId) {
        Interview interview = interviewRepository.findWithUserById(interviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_NOT_FOUND));

        if (!interview.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        if (interview.getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_IN_PROGRESS);
        }

        List<InterviewQuestion> questions = interviewQuestionRepository.findByInterviewIdOrderByQuestionOrderAsc(interviewId);
        Map<Long, InterviewAnswer> answersByQuestionId = findAnswersByQuestionId(interviewId);
        Long nextQuestionId = findNextQuestionId(questions, answersByQuestionId);
        boolean allAnswered = !questions.isEmpty() && questions.size() == answersByQuestionId.size();
        if (!allAnswered) {
            throw new BusinessException(ErrorCode.INTERVIEW_NOT_COMPLETABLE,
                    InterviewCompleteUnavailableResponse.builder()
                            .allAnswered(false)
                            .unansweredCount(questions.size() - answersByQuestionId.size())
                            .nextQuestionId(nextQuestionId)
                            .build());
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

    private InterviewHistoryResponse toHistoryResponse(Interview interview, Feedback feedback) {
        JobPosition jobPosition = interview.getJobPosition();

        return InterviewHistoryResponse.builder()
                .interviewId(interview.getId())
                .title(interview.getTitle())
                .status(interview.getStatus())
                .createdAt(interview.getCreatedAt())
                .completedAt(interview.getCompletedAt())
                .companyName(jobPosition == null ? null : jobPosition.getCompany().getName())
                .positionName(jobPosition == null ? null : jobPosition.getName())
                .overallScore(feedback == null ? null : feedback.getOverallScore())
                .build();
    }

    private Map<Long, InterviewAnswer> findAnswersByQuestionId(Long interviewId) {
        return interviewAnswerRepository.findAllByInterviewIdWithQuestion(interviewId).stream()
                .collect(Collectors.toMap(answer -> answer.getInterviewQuestion().getId(), Function.identity()));
    }

    private Long findNextQuestionId(List<InterviewQuestion> questions, Map<Long, InterviewAnswer> answersByQuestionId) {
        return questions.stream()
                .filter(question -> !answersByQuestionId.containsKey(question.getId()))
                .map(InterviewQuestion::getId)
                .findFirst()
                .orElse(null);
    }

    private InterviewProgressQuestionResponse toProgressQuestionResponse(InterviewQuestion question,
                                                                          InterviewAnswer answer) {
        return InterviewProgressQuestionResponse.builder()
                .questionId(question.getId())
                .parentQuestionId(question.getParentQuestion() == null ? null : question.getParentQuestion().getId())
                .questionOrder(question.getQuestionOrder())
                .content(question.getContent())
                .category(question.getCategory())
                .difficulty(question.getDifficulty())
                .answerContent(answer == null ? null : answer.getAnswerContent())
                .answeredAt(answer == null ? null : answer.getAnsweredAt())
                .build();
    }

    private InterviewFollowUpQuestionResponse createFollowUpQuestion(InterviewQuestion parentQuestion) {
        InterviewAnswer answer = interviewAnswerRepository.findByInterviewQuestionId(parentQuestion.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_ANSWER_NOT_FOUND));

        return openAiService.generateFollowUpQuestion(answer.getAnswerContent())
                .map(content -> {
                    InterviewQuestion followUpQuestion = interviewQuestionRepository.save(InterviewQuestion.builder()
                            .interview(parentQuestion.getInterview())
                            .parentQuestion(parentQuestion)
                            .questionOrder(interviewQuestionRepository.findMaxQuestionOrderByInterviewId(
                                    parentQuestion.getInterview().getId()) + 1)
                            .content(content)
                            .type(InterviewQuestionType.FOLLOW_UP)
                            .isAiGenerated(true)
                            .build());
                    return toFollowUpResponse(parentQuestion, followUpQuestion, true);
                })
                .orElseGet(() -> InterviewFollowUpQuestionResponse.builder()
                        .parentQuestionId(parentQuestion.getId())
                        .created(false)
                        .build());
    }

    private InterviewFollowUpQuestionResponse toFollowUpResponse(InterviewQuestion parentQuestion,
                                                                  InterviewQuestion followUpQuestion,
                                                                  boolean created) {
        return InterviewFollowUpQuestionResponse.builder()
                .parentQuestionId(parentQuestion.getId())
                .followUpQuestionId(followUpQuestion.getId())
                .content(followUpQuestion.getContent())
                .created(created)
                .build();
    }
}
