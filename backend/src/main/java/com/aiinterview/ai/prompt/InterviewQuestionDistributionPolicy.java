package com.aiinterview.ai.prompt;

import com.aiinterview.ai.dto.InterviewQuestionDistribution;
import com.aiinterview.interview.entity.Interview;
import com.aiinterview.interview.entity.InterviewQuestionCategory;
import com.aiinterview.interview.entity.InterviewQuestionDifficulty;
import com.aiinterview.jobposting.entity.JobPostingAnalysis;

import java.util.ArrayList;
import java.util.List;

public final class InterviewQuestionDistributionPolicy {

    public static final int DEFAULT_QUESTION_COUNT = 5;

    private InterviewQuestionDistributionPolicy() {
    }

    public static List<InterviewQuestionDistribution> create(Interview interview, int questionCount) {
        return create(interview, null, questionCount);
    }

    public static List<InterviewQuestionDistribution> create(Interview interview,
                                                              JobPostingAnalysis jobPostingAnalysis,
                                                              int questionCount) {
        List<InterviewQuestionDifficulty> difficulties = createDifficulties(questionCount);
        List<InterviewQuestionCategory> categories = createCategories(interview, jobPostingAnalysis, questionCount);

        List<InterviewQuestionDistribution> distributions = new ArrayList<>(questionCount);
        for (int index = 0; index < questionCount; index++) {
            distributions.add(new InterviewQuestionDistribution(index + 1, difficulties.get(index), categories.get(index)));
        }
        return distributions;
    }

    private static List<InterviewQuestionDifficulty> createDifficulties(int questionCount) {
        if (questionCount == DEFAULT_QUESTION_COUNT) {
            return List.of(
                    InterviewQuestionDifficulty.EASY,
                    InterviewQuestionDifficulty.MEDIUM,
                    InterviewQuestionDifficulty.MEDIUM,
                    InterviewQuestionDifficulty.MEDIUM,
                    InterviewQuestionDifficulty.HARD
            );
        }

        List<InterviewQuestionDifficulty> difficulties = new ArrayList<>(questionCount);
        for (int index = 0; index < questionCount; index++) {
            if (index == 0 && questionCount >= 3) {
                difficulties.add(InterviewQuestionDifficulty.EASY);
            } else if (index == questionCount - 1 && questionCount >= 2) {
                difficulties.add(InterviewQuestionDifficulty.HARD);
            } else {
                difficulties.add(InterviewQuestionDifficulty.MEDIUM);
            }
        }
        return difficulties;
    }

    private static List<InterviewQuestionCategory> createCategories(Interview interview,
                                                                     JobPostingAnalysis jobPostingAnalysis,
                                                                     int questionCount) {
        boolean hasJobPositionTechStack = interview.getJobPosition().getTechStack() != null
                && !interview.getJobPosition().getTechStack().isEmpty();
        boolean hasJobPostingTechStack = jobPostingAnalysis != null
                && jobPostingAnalysis.getTechStack() != null
                && !jobPostingAnalysis.getTechStack().isEmpty();
        boolean hasTechStack = hasJobPositionTechStack || hasJobPostingTechStack;
        boolean hasInterviewCriteria = interview.getJobPosition().getInterviewCriteria() != null
                && !interview.getJobPosition().getInterviewCriteria().isBlank();

        List<InterviewQuestionCategory> preferredCategories = new ArrayList<>();
        preferredCategories.add(InterviewQuestionCategory.CS);
        if (hasTechStack) {
            preferredCategories.add(InterviewQuestionCategory.TECH_STACK);
            preferredCategories.add(InterviewQuestionCategory.TECH_STACK);
        }
        preferredCategories.add(InterviewQuestionCategory.EXPERIENCE);
        preferredCategories.add(hasInterviewCriteria
                ? InterviewQuestionCategory.COMPANY_FIT
                : InterviewQuestionCategory.SITUATION);

        while (preferredCategories.size() < questionCount) {
            preferredCategories.add(hasInterviewCriteria
                    ? InterviewQuestionCategory.COMPANY_FIT
                    : InterviewQuestionCategory.SITUATION);
        }

        return preferredCategories.subList(0, questionCount);
    }
}
