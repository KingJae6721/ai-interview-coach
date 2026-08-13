export interface InterviewCreateRequest {
  jobPositionId: number;
  title: string;
}

export type InterviewStatus = "READY" | "IN_PROGRESS" | "COMPLETED" | "FAILED";

export interface InterviewCreateResponse {
  interviewId: number;
  title: string;
  status: InterviewStatus;
  questionCount: number;
}

export interface JobPositionResponse {
  jobPositionId: number;
  positionName: string;
  companyId: number;
  companyName: string;
  techStack: string[];
}
