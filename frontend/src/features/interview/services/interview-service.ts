import { apiFetch } from "@/services/api-client";
import type {
  InterviewCreateRequest,
  InterviewCreateResponse,
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
