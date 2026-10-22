import type { UserRole } from "@/features/auth/types/auth";

export type UserStatus = "ACTIVE" | "DELETED";
export type InterviewStatus =
  "READY" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";
export type QuestionCategory =
  "CS" | "TECH_STACK" | "EXPERIENCE" | "SITUATION" | "COMPANY_FIT";
export type QuestionDifficulty = "EASY" | "MEDIUM" | "HARD";
export type QuestionType = "NORMAL" | "FOLLOW_UP";

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface AdminInterviewListItem {
  interviewId: number;
  title: string;
  userId: number;
  userEmail: string;
  status: InterviewStatus;
  jobPostingSummary: string | null;
  questionCount: number;
  answeredQuestionCount: number;
  createdAt: string;
  completedAt: string | null;
}

export interface AdminDashboard {
  totalUsers: number;
  totalInterviews: number;
  readyInterviews: number;
  inProgressInterviews: number;
  completedInterviews: number;
  cancelledInterviews: number;
  todayCreatedInterviews: number;
  recentInterviews: AdminInterviewListItem[];
}

export interface AdminUserListItem {
  userId: number;
  email: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  interviewCount: number;
}

export interface AdminUserDetail {
  userId: number;
  email: string;
  nickname: string;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  interviewStatistics: {
    totalInterviews: number;
    readyInterviews: number;
    inProgressInterviews: number;
    completedInterviews: number;
    cancelledInterviews: number;
  };
  latestInterview: {
    interviewId: number;
    title: string;
    status: InterviewStatus;
    createdAt: string;
  } | null;
}

export interface AdminInterviewDetail {
  interviewId: number;
  title: string;
  status: InterviewStatus;
  userId: number;
  userEmail: string;
  jobPostingId: number | null;
  jobPostingTitle: string | null;
  jobPostingSummary: string | null;
  resumeUsed: boolean;
  resumeId: number | null;
  questions: Array<{
    questionId: number;
    parentQuestionId: number | null;
    questionOrder: number;
    questionContent: string;
    category: QuestionCategory;
    difficulty: QuestionDifficulty;
    type: QuestionType;
    answerExists: boolean;
    evaluationExists: boolean;
  }>;
  createdAt: string;
  startedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
}

export interface AdminAuditLog {
  auditLogId: number;
  actorUserId: number;
  targetType: "USER";
  targetId: number;
  action: "USER_ROLE_CHANGED";
  beforeValue: string;
  afterValue: string;
  createdAt: string;
}

export interface AdminUserFilters {
  email?: string;
  role?: UserRole;
  status?: UserStatus;
  page: number;
  size: number;
}

export interface AdminInterviewFilters {
  userEmail?: string;
  status?: InterviewStatus;
  page: number;
  size: number;
}

export interface AdminAuditLogFilters {
  action?: "USER_ROLE_CHANGED";
  actorUserId?: number;
  targetType?: "USER";
  fromDate?: string;
  toDate?: string;
  page: number;
  size: number;
}
