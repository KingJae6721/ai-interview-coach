"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";

import { getErrorMessage } from "@/features/auth/lib/get-error-message";
import { getDashboardSummary } from "@/features/dashboard/services/dashboard-service";
import type {
  DashboardRecentInterviewResponse,
  DashboardSummaryResponse,
} from "@/features/dashboard/types/dashboard";

type LoadState = "loading" | "success" | "error";

const STATUS_LABELS = {
  READY: "시작 전",
  IN_PROGRESS: "진행 중",
  COMPLETED: "완료",
  CANCELLED: "중도 종료",
} as const;

function formatDate(value: string | null): string {
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

function getInterviewHref(interview: DashboardRecentInterviewResponse) {
  if (interview.status === "COMPLETED") {
    return `/interviews/${interview.interviewId}/result`;
  }

  if (interview.status === "READY" || interview.status === "IN_PROGRESS") {
    return `/interviews/${interview.interviewId}`;
  }

  return null;
}

function RecentInterviewRow({
  interview,
}: {
  interview: DashboardRecentInterviewResponse;
}) {
  const href = getInterviewHref(interview);
  const content = (
    <>
      <div className="min-w-0">
        <div className="flex flex-wrap items-center gap-2">
          <h3 className="truncate font-medium text-zinc-900">
            {interview.title}
          </h3>
          <span className="rounded-full bg-zinc-100 px-2.5 py-1 text-xs font-medium text-zinc-600">
            {STATUS_LABELS[interview.status]}
          </span>
        </div>
        <p className="mt-1 text-sm text-zinc-500">
          {[interview.companyName, interview.positionName]
            .filter(Boolean)
            .join(" · ") || "직무 정보 없음"}
        </p>
        <p className="mt-1 text-xs text-zinc-400">
          {interview.status === "COMPLETED"
            ? `완료 ${formatDate(interview.completedAt)}`
            : interview.status === "CANCELLED"
              ? "중도 종료된 면접"
              : `생성 ${formatDate(interview.createdAt)}`}
        </p>
      </div>
      <div className="flex items-center justify-between gap-4 sm:justify-end">
        <span className="text-sm font-semibold text-zinc-900">
          {interview.overallScore !== null
            ? `${interview.overallScore}점`
            : interview.status === "CANCELLED"
              ? "면접 이력에서 확인"
              : interview.status === "COMPLETED"
                ? "결과 보기"
                : "계속하기"}
        </span>
        {href && (
          <span aria-hidden="true" className="text-zinc-400">
            →
          </span>
        )}
      </div>
    </>
  );

  return href ? (
    <Link
      href={href}
      className="flex flex-col gap-3 py-4 hover:bg-zinc-50 sm:flex-row sm:items-center sm:justify-between sm:px-3"
    >
      {content}
    </Link>
  ) : (
    <div className="flex flex-col gap-3 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-3">
      {content}
    </div>
  );
}

export function DashboardSummary() {
  const [loadState, setLoadState] = useState<LoadState>("loading");
  const [summary, setSummary] = useState<DashboardSummaryResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState("");

  const loadSummary = useCallback(async () => {
    setLoadState("loading");
    setErrorMessage("");

    try {
      setSummary(await getDashboardSummary());
      setLoadState("success");
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
      setLoadState("error");
    }
  }, []);

  useEffect(() => {
    let isActive = true;

    getDashboardSummary()
      .then((response) => {
        if (isActive) {
          setSummary(response);
          setLoadState("success");
        }
      })
      .catch((error: unknown) => {
        if (isActive) {
          setErrorMessage(getErrorMessage(error));
          setLoadState("error");
        }
      });

    return () => {
      isActive = false;
    };
  }, []);

  if (loadState === "loading") {
    return (
      <section
        role="status"
        className="mt-6 rounded-2xl border border-zinc-200 bg-white p-10 text-center shadow-sm"
      >
        <p className="text-sm font-medium text-zinc-600">
          면접 기록을 불러오는 중...
        </p>
      </section>
    );
  }

  if (loadState === "error" || !summary) {
    return (
      <section className="mt-6 rounded-2xl border border-red-200 bg-white p-8 text-center shadow-sm">
        <p role="alert" className="font-medium text-red-700">
          대시보드를 불러오지 못했습니다.
        </p>
        <p className="mt-2 text-sm text-red-600">{errorMessage}</p>
        <button
          type="button"
          onClick={() => void loadSummary()}
          className="mt-5 rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
        >
          다시 시도
        </button>
      </section>
    );
  }

  return (
    <div className="mt-6 space-y-6">
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <SummaryCard label="전체 면접" value={`${summary.totalInterviews}회`} />
        <SummaryCard
          label="완료 면접"
          value={`${summary.completedInterviews}회`}
        />
        <SummaryCard
          label="중도 종료"
          value={`${summary.cancelledInterviews}회`}
        />
        <SummaryCard
          label="평균 점수"
          value={
            summary.averageScore === null
              ? "-"
              : `${Math.round(summary.averageScore)}점`
          }
        />
        <SummaryCard
          label="최고 점수"
          value={
            summary.highestScore === null ? "-" : `${summary.highestScore}점`
          }
        />
      </section>

      <section className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-7">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 className="text-xl font-semibold text-zinc-900">최근 면접</h2>
            <p className="mt-1 text-sm text-zinc-500">
              최근 생성한 면접 5건을 확인할 수 있습니다.
            </p>
          </div>
          <Link
            href="/interviews/new"
            className="rounded-lg bg-zinc-900 px-4 py-2.5 text-center text-sm font-medium text-white hover:bg-zinc-700"
          >
            새 면접 만들기
          </Link>
        </div>

        {summary.recentInterviews.length === 0 ? (
          <div className="mt-6 rounded-xl border border-dashed border-zinc-300 bg-zinc-50 p-8 text-center">
            <p className="font-medium text-zinc-700">
              아직 면접 기록이 없습니다.
            </p>
            <p className="mt-2 text-sm text-zinc-500">
              첫 면접을 생성하면 이곳에서 진행 상태와 결과를 확인할 수 있습니다.
            </p>
          </div>
        ) : (
          <ul className="mt-5 divide-y divide-zinc-200">
            {summary.recentInterviews.map((interview) => (
              <li key={interview.interviewId}>
                <RecentInterviewRow interview={interview} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}

function SummaryCard({ label, value }: { label: string; value: string }) {
  return (
    <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
      <p className="text-sm text-zinc-500">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-zinc-900">{value}</p>
    </article>
  );
}
