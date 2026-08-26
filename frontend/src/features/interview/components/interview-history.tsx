"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { getErrorMessage } from "@/features/auth/lib/get-error-message";
import { getInterviewHistory } from "@/features/interview/services/interview-service";
import type {
  InterviewHistoryResponse,
  PageResponse,
} from "@/features/interview/types/interview";

const PAGE_SIZE = 10;

const STATUS_LABELS = {
  READY: "시작 전",
  IN_PROGRESS: "진행 중",
  COMPLETED: "완료",
  CANCELLED: "중도 종료",
} as const;

const STATUS_STYLES = {
  READY: "bg-zinc-100 text-zinc-700",
  IN_PROGRESS: "bg-blue-100 text-blue-700",
  COMPLETED: "bg-emerald-100 text-emerald-700",
  CANCELLED: "bg-amber-100 text-amber-800",
} as const;

function formatDateTime(value: string | null): string {
  if (!value) {
    return "-";
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? value
    : new Intl.DateTimeFormat("ko-KR", {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(date);
}

function getCta(interview: InterviewHistoryResponse) {
  if (interview.status === "COMPLETED" && interview.feedbackExists) {
    return {
      href: `/interviews/${interview.interviewId}/result`,
      label: "결과 보기",
    };
  }

  if (interview.status === "IN_PROGRESS") {
    return {
      href: `/interviews/${interview.interviewId}`,
      label: "면접 계속하기",
    };
  }

  if (interview.status === "READY") {
    return {
      href: `/interviews/${interview.interviewId}`,
      label: "면접 시작하기",
    };
  }

  if (
    interview.status === "CANCELLED" &&
    interview.feedbackExists &&
    interview.partial
  ) {
    return {
      href: `/interviews/${interview.interviewId}/result`,
      label: "부분 결과 보기",
    };
  }

  return null;
}

function getNoCtaMessage(interview: InterviewHistoryResponse): string {
  if (interview.status === "CANCELLED") {
    return "중도 종료됨";
  }

  if (interview.status === "COMPLETED" && !interview.feedbackExists) {
    return "피드백이 아직 생성되지 않았습니다.";
  }

  return "이동할 수 있는 화면이 없습니다.";
}

function getScoreLabel(interview: InterviewHistoryResponse): string {
  if (interview.overallScore !== null) {
    return `종합 점수 ${interview.overallScore}점`;
  }

  return interview.partial ? "부분 피드백 제공" : "점수 없음";
}

export function InterviewHistory() {
  const [page, setPage] = useState(0);
  const [history, setHistory] =
    useState<PageResponse<InterviewHistoryResponse> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    let isActive = true;

    getInterviewHistory(page, PAGE_SIZE)
      .then((response) => {
        if (isActive) {
          setHistory(response);
        }
      })
      .catch((error: unknown) => {
        if (isActive) {
          setErrorMessage(getErrorMessage(error));
        }
      })
      .finally(() => {
        if (isActive) {
          setIsLoading(false);
        }
      });

    return () => {
      isActive = false;
    };
  }, [page, retryCount]);

  if (isLoading) {
    return (
      <section
        role="status"
        className="rounded-2xl border border-zinc-200 bg-white p-10 text-center shadow-sm"
      >
        <p className="text-sm font-medium text-zinc-600">
          면접 이력을 불러오는 중입니다.
        </p>
      </section>
    );
  }

  if (errorMessage || !history) {
    return (
      <section className="rounded-2xl border border-red-200 bg-white p-8 text-center shadow-sm">
        <h1 className="text-xl font-semibold text-red-800">
          면접 이력을 불러오지 못했습니다.
        </h1>
        <p role="alert" className="mt-3 text-sm text-red-600">
          {errorMessage}
        </p>
        <button
          type="button"
          onClick={() => {
            setIsLoading(true);
            setErrorMessage("");
            setRetryCount((count) => count + 1);
          }}
          className="mt-6 rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
        >
          다시 시도
        </button>
      </section>
    );
  }

  if (history.empty) {
    return (
      <section className="rounded-2xl border border-dashed border-zinc-300 bg-white p-10 text-center shadow-sm">
        <h1 className="text-xl font-semibold text-zinc-900">
          아직 면접 이력이 없습니다.
        </h1>
        <p className="mt-3 text-sm leading-6 text-zinc-500">
          새 면접을 만들고 AI 면접 연습을 시작해 보세요.
        </p>
        <Link
          href="/interviews/new"
          className="mt-6 inline-flex rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
        >
          새 면접 만들기
        </Link>
      </section>
    );
  }

  return (
    <div className="space-y-4">
      <p className="text-sm text-zinc-500">
        총 {history.totalElements}개의 면접 이력
      </p>
      <ul className="space-y-3">
        {history.content.map((interview) => {
          const cta = getCta(interview);

          return (
            <li
              key={interview.interviewId}
              className={`rounded-2xl border bg-white p-5 shadow-sm sm:p-6 ${
                interview.status === "CANCELLED"
                  ? "border-amber-200"
                  : "border-zinc-200"
              }`}
            >
              <div className="flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-xs font-medium text-zinc-400">
                      #{interview.interviewId}
                    </span>
                    <span
                      className={`rounded-full px-2.5 py-1 text-xs font-semibold ${STATUS_STYLES[interview.status]}`}
                    >
                      {STATUS_LABELS[interview.status]}
                    </span>
                    {interview.partial && (
                      <span className="rounded-full bg-violet-100 px-2.5 py-1 text-xs font-semibold text-violet-700">
                        부분 피드백
                      </span>
                    )}
                  </div>
                  <h2 className="mt-3 truncate text-lg font-semibold text-zinc-900">
                    {interview.title}
                  </h2>
                  <p className="mt-1 text-sm text-zinc-600">
                    {[interview.companyName, interview.positionName]
                      .filter(Boolean)
                      .join(" · ") || "회사 및 직무 정보 없음"}
                  </p>
                  <dl className="mt-4 grid gap-2 text-xs text-zinc-500 sm:grid-cols-2">
                    <div>
                      <dt className="sr-only">생성 일시</dt>
                      <dd>생성 {formatDateTime(interview.createdAt)}</dd>
                    </div>
                    {interview.startedAt && (
                      <div>
                        <dt className="sr-only">시작 일시</dt>
                        <dd>시작 {formatDateTime(interview.startedAt)}</dd>
                      </div>
                    )}
                    {interview.completedAt && (
                      <div>
                        <dt className="sr-only">완료 일시</dt>
                        <dd>완료 {formatDateTime(interview.completedAt)}</dd>
                      </div>
                    )}
                    {interview.cancelledAt && (
                      <div>
                        <dt className="sr-only">중도 종료 일시</dt>
                        <dd>
                          중도 종료 {formatDateTime(interview.cancelledAt)}
                        </dd>
                      </div>
                    )}
                  </dl>
                </div>

                <div className="flex shrink-0 flex-col gap-3 sm:items-end">
                  <p className="text-sm font-semibold text-zinc-900">
                    {getScoreLabel(interview)}
                  </p>
                  {cta ? (
                    <Link
                      href={cta.href}
                      className={`rounded-lg px-4 py-2.5 text-center text-sm font-medium text-white ${
                        interview.status === "CANCELLED"
                          ? "bg-amber-700 hover:bg-amber-600"
                          : "bg-zinc-900 hover:bg-zinc-700"
                      }`}
                    >
                      {cta.label}
                    </Link>
                  ) : (
                    <p
                      className={`max-w-52 text-right text-xs leading-5 ${
                        interview.status === "CANCELLED"
                          ? "font-medium text-amber-700"
                          : "text-zinc-500"
                      }`}
                    >
                      {getNoCtaMessage(interview)}
                    </p>
                  )}
                </div>
              </div>
            </li>
          );
        })}
      </ul>

      <nav
        aria-label="면접 이력 페이지"
        className="flex items-center justify-between pt-2"
      >
        <button
          type="button"
          disabled={history.first}
          onClick={() => {
            setIsLoading(true);
            setErrorMessage("");
            setPage((currentPage) => Math.max(0, currentPage - 1));
          }}
          className="rounded-lg border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-40"
        >
          이전
        </button>
        <span className="text-sm text-zinc-500">
          {history.number + 1} / {Math.max(history.totalPages, 1)} 페이지
        </span>
        <button
          type="button"
          disabled={history.last}
          onClick={() => {
            setIsLoading(true);
            setErrorMessage("");
            setPage((currentPage) => currentPage + 1);
          }}
          className="rounded-lg border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-40"
        >
          다음
        </button>
      </nav>
    </div>
  );
}
