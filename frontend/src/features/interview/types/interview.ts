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

export interface InterviewStartResponse {
  interviewId: number;
  status: "IN_PROGRESS";
  startedAt: string;
}

export interface JobPositionResponse {
  jobPositionId: number;
  positionName: string;
  companyId: number;
  companyName: string;
  techStack: string[];
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

export interface FeedbackGenerateResponse {
  feedbackId: number;
  interviewId: number;
  overallScore: number;
  strengths: string;
  weaknesses: string;
  improvementSuggestions: string;
  summary: string;
}

export interface InterviewResultFeedbackResponse {
  overallScore: number;
  strengths: string;
  weaknesses: string;
  improvementSuggestions: string;
  summary: string;
}

export interface InterviewResultQuestionAnswerResponse {
  questionOrder: number;
  questionContent: string;
  answerContent: string;
  answeredAt: string;
}

export interface InterviewResultResponse {
  interviewId: number;
  title: string;
  status: "COMPLETED";
  questionAnswers: InterviewResultQuestionAnswerResponse[];
  feedback: InterviewResultFeedbackResponse;
}
