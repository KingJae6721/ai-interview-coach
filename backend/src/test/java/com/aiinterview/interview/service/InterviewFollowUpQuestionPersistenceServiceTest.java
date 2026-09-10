package com.aiinterview.interview.service;

import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewQuestion;
import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import com.aiinterview.interview.repository.InterviewQuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class InterviewFollowUpQuestionPersistenceServiceTest {

    @Mock
    private InterviewQuestionRepository interviewQuestionRepository;

    @Test
    void save_inheritsCategoryAndDifficultyFromParentQuestion() {
        Interview interview = mock(Interview.class);
        InterviewQuestion parentQuestion = mock(InterviewQuestion.class);
        given(interview.getId()).willReturn(10L);
        given(parentQuestion.getInterview()).willReturn(interview);
        given(parentQuestion.getCategory()).willReturn(InterviewQuestionCategory.EXPERIENCE);
        given(parentQuestion.getDifficulty()).willReturn(InterviewQuestionDifficulty.HARD);
        given(interviewQuestionRepository.findWithInterviewAndUserById(1L))
                .willReturn(Optional.of(parentQuestion));
        given(interviewQuestionRepository.findMaxQuestionOrderByInterviewId(10L)).willReturn(5);

        new InterviewFollowUpQuestionPersistenceService(interviewQuestionRepository)
                .save(1L, "follow-up question");

        ArgumentCaptor<InterviewQuestion> questionCaptor = ArgumentCaptor.forClass(InterviewQuestion.class);
        then(interviewQuestionRepository).should().saveAndFlush(questionCaptor.capture());
        assertThat(questionCaptor.getValue().getParentQuestion()).isEqualTo(parentQuestion);
        assertThat(questionCaptor.getValue().getCategory()).isEqualTo(InterviewQuestionCategory.EXPERIENCE);
        assertThat(questionCaptor.getValue().getDifficulty()).isEqualTo(InterviewQuestionDifficulty.HARD);
    }
}
