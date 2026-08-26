"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { getErrorMessage } from "@/features/auth/lib/get-error-message";
import {
  getDashboardAnalytics,
  getDashboardScoreTrend,
  getDashboardWeaknesses,
} from "@/features/dashboard/services/dashboard-service";
import type {
  DashboardAnalyticsPeriod,
  DashboardAnalyticsResponse,
  DashboardCategoryStatisticsResponse,
  DashboardDifficultyStatisticsResponse,
  DashboardScoreTrendResponse,
  DashboardWeaknessResponse,
} from "@/features/dashboard/types/dashboard";

const CATEGORY_LABELS: Record<string, string> = {
  CS: "CS 기초",
  TECH_STACK: "기술 스택",
  EXPERIENCE: "경험",
  SITUATION: "상황 대응",
  COMPANY_FIT: "회사 적합도",
};

const DIFFICULTY_LABELS: Record<string, string> = {
  EASY: "쉬움",
  MEDIUM: "보통",
  HARD: "어려움",
};

function formatDate(value: string, options?: Intl.DateTimeFormatOptions) {
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? value
    : new Intl.DateTimeFormat(
        "ko-KR",
        options ?? { dateStyle: "medium" },
      ).format(date);
}

function ScoreTrendChart({ data }: { data: DashboardScoreTrendResponse[] }) {
  const points = data.map((item, index) => {
    const x = data.length === 1 ? 300 : 24 + (index / (data.length - 1)) * 552;
    const y = 20 + (100 - item.overallScore) * 1.6;
    return { ...item, x, y };
  });

  return (
    <div className="mt-6 overflow-x-auto">
      <svg
        viewBox="0 0 600 220"
        role="img"
        aria-label="최근 완료 면접 점수 추이"
        className="min-w-[560px]"
      >
        {[0, 25, 50, 75, 100].map((score) => {
          const y = 20 + (100 - score) * 1.6;
          return (
            <g key={score}>
              <line x1="24" x2="576" y1={y} y2={y} stroke="#e4e4e7" />
              <text x="2" y={y + 4} fontSize="10" fill="#71717a">
                {score}
              </text>
            </g>
          );
        })}
        {points.length > 1 && (
          <polyline
            fill="none"
            stroke="#18181b"
            strokeWidth="3"
            strokeLinejoin="round"
            strokeLinecap="round"
            points={points.map(({ x, y }) => `${x},${y}`).join(" ")}
          />
        )}
        {points.map((point) => (
          <g key={point.interviewId}>
            <circle cx={point.x} cy={point.y} r="5" fill="#18181b">
              <title>{`${point.title}: ${point.overallScore}점`}</title>
            </circle>
            <text
              x={point.x}
              y={point.y - 10}
              textAnchor="middle"
              fontSize="11"
              fontWeight="600"
              fill="#18181b"
            >
              {point.overallScore}
            </text>
            <text
              x={point.x}
              y="210"
              textAnchor="middle"
              fontSize="10"
              fill="#71717a"
            >
              {formatDate(point.completedAt, {
                month: "numeric",
                day: "numeric",
              })}
            </text>
          </g>
        ))}
      </svg>
    </div>
  );
}

type WeaknessStatistic =
  DashboardCategoryStatisticsResponse | DashboardDifficultyStatisticsResponse;

function StatisticsList({
  title,
  items,
  getLabel,
}: {
  title: string;
  items: WeaknessStatistic[];
  getLabel: (item: WeaknessStatistic) => string;
}) {
  return (
    <section>
      <h3 className="font-semibold text-zinc-900">{title}</h3>
      <ul className="mt-3 space-y-3">
        {items.map((item) => {
          const key = "category" in item ? item.category : item.difficulty;
          const score = item.averageScore ?? 0;
          return (
            <li key={key} className="rounded-xl bg-zinc-50 p-4">
              <div className="flex items-center justify-between gap-3 text-sm">
                <span className="font-medium text-zinc-800">
                  {getLabel(item)}
                </span>
                <strong className="text-zinc-900">
                  {item.averageScore === null
                    ? "점수 없음"
                    : `${item.averageScore.toFixed(1)}점`}
                </strong>
              </div>
              <div className="mt-2 h-2 overflow-hidden rounded-full bg-zinc-200">
                <div
                  className="h-full rounded-full bg-zinc-800"
                  style={{ width: `${Math.max(0, Math.min(100, score))}%` }}
                />
              </div>
              <p className="mt-2 text-xs text-zinc-500">
                면접 {item.interviewCount}건 · 질문 {item.questionCount}개 ·
                평가 {item.evaluationCount}개
              </p>
              {item.evaluationCount === 0 && (
                <p className="mt-1 text-xs font-medium text-amber-700">
                  평가 데이터가 없어 약점 판단에서 제외됩니다.
                </p>
              )}
            </li>
          );
        })}
      </ul>
    </section>
  );
}

export function DashboardInsights() {
  const [trend, setTrend] = useState<DashboardScoreTrendResponse[] | null>(
    null,
  );
  const [analytics, setAnalytics] = useState<
    DashboardAnalyticsResponse[] | null
  >(null);
  const [weakness, setWeakness] = useState<DashboardWeaknessResponse | null>(
    null,
  );
  const [period, setPeriod] = useState<DashboardAnalyticsPeriod>("WEEKLY");
  const [errorMessage, setErrorMessage] = useState("");
  const [isAnalyticsLoading, setIsAnalyticsLoading] = useState(false);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    let isActive = true;

    Promise.all([getDashboardScoreTrend(), getDashboardWeaknesses()])
      .then(([trendResponse, weaknessResponse]) => {
        if (isActive) {
          setTrend(trendResponse);
          setWeakness(weaknessResponse);
        }
      })
      .catch((error: unknown) => {
        if (isActive) {
          setErrorMessage(getErrorMessage(error));
        }
      });

    return () => {
      isActive = false;
    };
  }, [retryCount]);

  useEffect(() => {
    let isActive = true;

    getDashboardAnalytics(period)
      .then((response) => {
        if (isActive) {
          setAnalytics(response);
        }
      })
      .catch((error: unknown) => {
        if (isActive) {
          setAnalytics(null);
          setErrorMessage(getErrorMessage(error));
        }
      })
      .finally(() => {
        if (isActive) {
          setIsAnalyticsLoading(false);
        }
      });

    return () => {
      isActive = false;
    };
  }, [period, retryCount]);

  function handlePeriodChange(nextPeriod: DashboardAnalyticsPeriod) {
    if (nextPeriod === period || isAnalyticsLoading) return;
    setPeriod(nextPeriod);
    setIsAnalyticsLoading(true);
    setErrorMessage("");
  }

  if (errorMessage && (!trend || !analytics || !weakness)) {
    return (
      <section className="rounded-2xl border border-red-200 bg-white p-8 text-center shadow-sm">
        <p role="alert" className="font-medium text-red-700">
          성장 분석을 불러오지 못했습니다.
        </p>
        <p className="mt-2 text-sm text-red-600">{errorMessage}</p>
        <button
          type="button"
          onClick={() => {
            setErrorMessage("");
            setRetryCount((count) => count + 1);
          }}
          className="mt-5 rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
        >
          다시 시도
        </button>
      </section>
    );
  }

  if (!trend || !analytics || !weakness) {
    return (
      <section
        role="status"
        className="rounded-2xl border border-zinc-200 bg-white p-10 text-center shadow-sm"
      >
        <p className="text-sm font-medium text-zinc-600">
          성장 분석을 불러오는 중...
        </p>
      </section>
    );
  }

  return (
    <>
      <section className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-7">
        <div className="flex items-end justify-between gap-4">
          <div>
            <h2 className="text-xl font-semibold text-zinc-900">점수 추이</h2>
            <p className="mt-1 text-sm text-zinc-500">
              최근 완료 면접 최대 10건 기준입니다.
            </p>
          </div>
          <Link
            href="/interviews"
            className="text-sm font-medium text-zinc-700 underline underline-offset-4"
          >
            면접 이력 보기
          </Link>
        </div>
        {trend.length === 0 ? (
          <p className="mt-6 rounded-xl bg-zinc-50 p-8 text-center text-sm text-zinc-500">
            점수 추이를 표시할 완료 면접이 없습니다.
          </p>
        ) : (
          <>
            {trend.length === 1 && (
              <p className="mt-4 text-sm text-amber-700">
                면접이 1건이라 변화 추이보다 현재 점수만 표시합니다.
              </p>
            )}
            <ScoreTrendChart data={trend} />
          </>
        )}
      </section>

      <section className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-7">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-xl font-semibold text-zinc-900">기간별 분석</h2>
            <p className="mt-1 text-sm text-zinc-500">
              완료 면접 점수를 기간 단위로 집계합니다.
            </p>
          </div>
          <div
            className="flex rounded-xl bg-zinc-100 p-1"
            aria-label="분석 기간"
          >
            {(["WEEKLY", "MONTHLY"] as const).map((item) => (
              <button
                key={item}
                type="button"
                disabled={isAnalyticsLoading}
                onClick={() => handlePeriodChange(item)}
                className={`rounded-lg px-4 py-2 text-sm font-medium ${period === item ? "bg-white text-zinc-900 shadow-sm" : "text-zinc-500"}`}
              >
                {item === "WEEKLY" ? "주간" : "월간"}
              </button>
            ))}
          </div>
        </div>
        {errorMessage && (
          <p role="alert" className="mt-4 text-sm text-red-600">
            {errorMessage}
          </p>
        )}
        {isAnalyticsLoading ? (
          <p role="status" className="mt-6 text-center text-sm text-zinc-500">
            기간별 분석을 불러오는 중...
          </p>
        ) : analytics.length === 0 ? (
          <p className="mt-6 rounded-xl bg-zinc-50 p-8 text-center text-sm text-zinc-500">
            선택한 기간의 분석 데이터가 없습니다.
          </p>
        ) : (
          <div className="mt-6 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {analytics.map((item) => (
              <article
                key={item.periodStartAt}
                className="rounded-xl border border-zinc-200 p-4"
              >
                <p className="text-xs text-zinc-500">
                  {formatDate(item.periodStartAt)}
                </p>
                <p className="mt-2 text-2xl font-semibold text-zinc-900">
                  {item.averageScore.toFixed(1)}점
                </p>
                <div className="mt-2 flex justify-between text-xs text-zinc-500">
                  <span>면접 {item.interviewCount}건</span>
                  <span
                    className={
                      item.scoreChange === null
                        ? ""
                        : item.scoreChange >= 0
                          ? "text-emerald-700"
                          : "text-red-600"
                    }
                  >
                    {item.scoreChange === null
                      ? "비교 없음"
                      : `${item.scoreChange >= 0 ? "+" : ""}${item.scoreChange.toFixed(1)}점`}
                  </span>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-7">
        <h2 className="text-xl font-semibold text-zinc-900">약점 분석</h2>
        {!weakness.performanceAnalysisAvailable ? (
          <div className="mt-5 rounded-xl border border-dashed border-zinc-300 bg-zinc-50 p-8 text-center">
            <p className="font-medium text-zinc-700">
              아직 분석할 평가 데이터가 없습니다.
            </p>
            <p className="mt-2 text-sm text-zinc-500">
              질문별 평가가 쌓이면 카테고리와 난이도별 성과를 확인할 수
              있습니다.
            </p>
          </div>
        ) : (
          <>
            <div className="mt-5 grid gap-3 sm:grid-cols-2">
              <article className="rounded-xl bg-rose-50 p-4">
                <p className="text-xs text-rose-700">가장 약한 카테고리</p>
                <p className="mt-1 font-semibold text-rose-900">
                  {weakness.weakestCategory
                    ? (CATEGORY_LABELS[weakness.weakestCategory] ??
                      weakness.weakestCategory)
                    : "-"}
                </p>
              </article>
              <article className="rounded-xl bg-amber-50 p-4">
                <p className="text-xs text-amber-700">가장 약한 난이도</p>
                <p className="mt-1 font-semibold text-amber-900">
                  {weakness.weakestDifficulty
                    ? (DIFFICULTY_LABELS[weakness.weakestDifficulty] ??
                      weakness.weakestDifficulty)
                    : "-"}
                </p>
              </article>
            </div>
            <div className="mt-6 grid gap-7 lg:grid-cols-2">
              <StatisticsList
                title="카테고리별 성과"
                items={weakness.categoryStatistics}
                getLabel={(item) =>
                  "category" in item
                    ? (CATEGORY_LABELS[item.category] ?? item.category)
                    : ""
                }
              />
              <StatisticsList
                title="난이도별 성과"
                items={weakness.difficultyStatistics}
                getLabel={(item) =>
                  "difficulty" in item
                    ? (DIFFICULTY_LABELS[item.difficulty] ?? item.difficulty)
                    : ""
                }
              />
            </div>
          </>
        )}
      </section>
    </>
  );
}
