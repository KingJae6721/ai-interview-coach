import { apiFetch } from "@/services/api-client";
import type {
  FeedbackGenerateResponse,
  InterviewAnswerCreateRequest,
  InterviewAnswerCreateResponse,
  InterviewCancelResponse,
  InterviewCompleteResponse,
  InterviewCreateRequest,
  InterviewCreateResponse,
  InterviewFollowUpQuestionResponse,
  InterviewHistoryResponse,
  InterviewProgressResponse,
  InterviewResultResponse,
  InterviewStartResponse,
  InterviewStateResponse,
  JobPostingAnalyzeRequest,
  JobPostingAnalyzeResponse,
  JobPostingSummaryResponse,
  PageResponse,
  ResumeAnalyzeResponse,
  ResumeSummaryResponse,
} from "@/features/interview/types/interview";

export function analyzeJobPosting(
  request: JobPostingAnalyzeRequest,
): Promise<JobPostingAnalyzeResponse> {
  return apiFetch<JobPostingAnalyzeResponse>("/api/v1/job-postings/analyze", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function getJobPostings(): Promise<JobPostingSummaryResponse[]> {
  return apiFetch<JobPostingSummaryResponse[]>("/api/v1/job-postings");
}

export function getResumes(): Promise<ResumeSummaryResponse[]> {
  return apiFetch<ResumeSummaryResponse[]>("/api/v1/resumes");
}

export function analyzeResume(file: File): Promise<ResumeAnalyzeResponse> {
  const formData = new FormData();
  formData.append("file", file);

  return apiFetch<ResumeAnalyzeResponse>("/api/v1/resumes/analyze", {
    method: "POST",
    body: formData,
  });
}

export function createInterview(
  request: InterviewCreateRequest,
): Promise<InterviewCreateResponse> {
  return apiFetch<InterviewCreateResponse>("/api/v1/interviews", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function getInterviewHistory(
  page: number,
  size: number,
): Promise<PageResponse<InterviewHistoryResponse>> {
  const searchParams = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  return apiFetch<PageResponse<InterviewHistoryResponse>>(
    `/api/v1/interviews?${searchParams.toString()}`,
  );
}

export function startInterview(
  interviewId: number,
): Promise<InterviewStartResponse> {
  return apiFetch<InterviewStartResponse>(
    `/api/v1/interviews/${interviewId}/start`,
    { method: "POST" },
  );
}

export function getInterviewState(
  interviewId: number,
): Promise<InterviewStateResponse> {
  return apiFetch<InterviewStateResponse>(`/api/v1/interviews/${interviewId}`);
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

export function cancelInterview(
  interviewId: number,
): Promise<InterviewCancelResponse> {
  return apiFetch<InterviewCancelResponse>(
    `/api/v1/interviews/${interviewId}/cancel`,
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
