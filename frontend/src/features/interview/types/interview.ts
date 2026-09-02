export interface InterviewCreateRequest {
  title: string;
  jobPostingId: number;
  resumeId?: number;
}

export type InterviewStatus =
  "READY" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";

export interface InterviewCreateResponse {
  interviewId: number;
  title: string;
  status: InterviewStatus;
  questionCount: number;
}

export interface InterviewStartResponse {
  interviewId: number;
  status: "IN_PROGRESS";
  startedAt: string;
}

export interface InterviewStateResponse {
  interviewId: number;
  title: string;
  status: InterviewStatus;
  createdAt: string;
  startedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
  positionName: string | null;
  companyName: string | null;
}

export interface JobPostingAnalyzeRequest {
  postingUrl: string;
}

export interface JobPostingAnalyzeResponse {
  jobPostingId: number;
  postingUrl: string;
  title: string;
  companyName: string;
  positionName: string;
  responsibilities: string[];
  requiredQualifications: string[];
  preferredQualifications: string[];
  techStack: string[];
  experienceRequirements: string[];
  keywords: string[];
  summary: string;
  analyzedAt: string;
}

export interface JobPostingSummaryResponse {
  jobPostingId: number;
  postingUrl: string;
  companyName: string;
  positionName: string;
  summary: string;
  techStack: string[];
  analyzedAt: string;
}

export interface ResumeSummaryResponse {
  resumeId: number;
  originalFileName: string;
  createdAt: string;
  summary: string;
  skills: string[];
}

export interface ResumeAnalyzeResponse extends ResumeSummaryResponse {
  fileSize: number;
  workExperiences: string[];
  projects: string[];
  education: string[];
  certifications: string[];
  achievements: string[];
  strengths: string[];
  keywords: string[];
  analyzedAt: string;
}

export type QuestionCategory =
  "CS" | "TECH_STACK" | "EXPERIENCE" | "SITUATION" | "COMPANY_FIT";

export type QuestionDifficulty = "EASY" | "MEDIUM" | "HARD";

export interface InterviewProgressQuestionResponse {
  questionId: number;
  parentQuestionId: number | null;
  questionOrder: number;
  content: string;
  category: QuestionCategory | null;
  difficulty: QuestionDifficulty | null;
  answerContent: string | null;
  answeredAt: string | null;
}

export interface InterviewProgressResponse {
  interviewId: number;
  status: InterviewStatus;
  questions: InterviewProgressQuestionResponse[];
  nextQuestionId: number | null;
  allAnswered: boolean;
}

export interface InterviewAnswerCreateRequest {
  answerContent: string;
}

export interface InterviewAnswerCreateResponse {
  answerId: number;
  questionId: number;
  answerContent: string;
  answeredAt: string;
  created: boolean;
}

export interface InterviewFollowUpQuestionResponse {
  parentQuestionId: number;
  followUpQuestionId: number | null;
  content: string | null;
  created: boolean;
}

export interface InterviewCompleteResponse {
  interviewId: number;
  status: "COMPLETED";
  completedAt: string;
}

export interface InterviewCancelResponse {
  interviewId: number;
  status: "CANCELLED";
  cancelledAt: string;
}

export interface FeedbackGenerateResponse {
  feedbackId: number;
  interviewId: number;
  overallScore: number | null;
  partial: boolean;
  answeredCount: number;
  totalQuestionCount: number;
  strengths: string;
  weaknesses: string;
  improvementSuggestions: string;
  summary: string;
}

export interface InterviewResultFeedbackResponse {
  overallScore: number | null;
  partial: boolean;
  answeredCount: number;
  totalQuestionCount: number;
  strengths: string;
  weaknesses: string;
  improvementSuggestions: string;
  summary: string;
}

export interface InterviewResultQuestionAnswerResponse {
  questionId: number;
  parentQuestionId: number | null;
  questionOrder: number;
  questionContent: string;
  category: QuestionCategory | null;
  difficulty: QuestionDifficulty | null;
  followUp: boolean;
  answerContent: string | null;
  answeredAt: string | null;
  evaluation: InterviewResultQuestionEvaluationResponse | null;
}

export interface InterviewResultQuestionEvaluationResponse {
  evaluationId: number;
  score: number;
  strengths: string;
  weaknesses: string;
  improvementSuggestion: string;
  reasoning: string;
}

export interface InterviewResultResponse {
  interviewId: number;
  title: string;
  status: "COMPLETED" | "CANCELLED";
  completedAt: string | null;
  cancelledAt: string | null;
  companyName: string | null;
  positionName: string | null;
  questionAnswers: InterviewResultQuestionAnswerResponse[];
  feedback: InterviewResultFeedbackResponse;
}

export interface InterviewHistoryResponse {
  interviewId: number;
  title: string;
  status: InterviewStatus;
  createdAt: string;
  startedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
  companyName: string | null;
  positionName: string | null;
  overallScore: number | null;
  feedbackExists: boolean;
  partial: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
