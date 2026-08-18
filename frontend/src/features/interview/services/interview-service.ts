import { apiFetch } from "@/services/api-client";
import type {
  FeedbackGenerateResponse,
  InterviewAnswerCreateRequest,
  InterviewAnswerCreateResponse,
  InterviewCompleteResponse,
  InterviewCreateRequest,
  InterviewCreateResponse,
  InterviewFollowUpQuestionResponse,
  InterviewProgressResponse,
  InterviewResultResponse,
  InterviewStartResponse,
  JobPositionResponse,
} from "@/features/interview/types/interview";

export function getJobPositions(): Promise<JobPositionResponse[]> {
  return apiFetch<JobPositionResponse[]>("/api/v1/job-positions");
}

export function createInterview(
  request: InterviewCreateRequest,
): Promise<InterviewCreateResponse> {
  return apiFetch<InterviewCreateResponse>("/api/v1/interviews", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function startInterview(
  interviewId: number,
): Promise<InterviewStartResponse> {
  return apiFetch<InterviewStartResponse>(
    `/api/v1/interviews/${interviewId}/start`,
    { method: "POST" },
  );
}

export function getInterviewProgress(
  interviewId: number,
): Promise<InterviewProgressResponse> {
  return apiFetch<InterviewProgressResponse>(
    `/api/v1/interviews/${interviewId}/progress`,
  );
}

export function submitInterviewAnswer(
  questionId: number,
  request: InterviewAnswerCreateRequest,
): Promise<InterviewAnswerCreateResponse> {
  return apiFetch<InterviewAnswerCreateResponse>(
    `/api/v1/interviews/questions/${questionId}/answers`,
    {
      method: "POST",
      body: JSON.stringify(request),
    },
  );
}

export function generateFollowUpQuestion(
  questionId: number,
): Promise<InterviewFollowUpQuestionResponse> {
  return apiFetch<InterviewFollowUpQuestionResponse>(
    `/api/v1/ai/questions/${questionId}/follow-up`,
    { method: "POST" },
  );
}

export function completeInterview(
  interviewId: number,
): Promise<InterviewCompleteResponse> {
  return apiFetch<InterviewCompleteResponse>(
    `/api/v1/interviews/${interviewId}/complete`,
    { method: "POST" },
  );
}

export function generateInterviewFeedback(
  interviewId: number,
): Promise<FeedbackGenerateResponse> {
  return apiFetch<FeedbackGenerateResponse>(
    `/api/v1/interviews/${interviewId}/feedback`,
    { method: "POST" },
  );
}

export function getInterviewResult(
  interviewId: number,
): Promise<InterviewResultResponse> {
  return apiFetch<InterviewResultResponse>(
    `/api/v1/interviews/${interviewId}/result`,
  );
}
