"use client";

import Link from "next/link";
import { useState } from "react";

import { useAuth } from "@/features/auth/context/auth-context";
import { getErrorMessage } from "@/features/auth/lib/get-error-message";

interface AuthStatusProps {
  showInterviewCta?: boolean;
  showDashboardCta?: boolean;
  interviewCtaLabel?: string;
  compact?: boolean;
}

export function AuthStatus({
  showInterviewCta = true,
  showDashboardCta = true,
  interviewCtaLabel = "면접 준비하기",
  compact = false,
}: AuthStatusProps) {
  const { user, isInitialized, logout } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  async function handleLogout() {
    setIsLoading(true);
    setErrorMessage("");

    try {
      await logout();
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }

  if (!isInitialized) {
    return <p className="mt-8 text-sm text-zinc-500">인증 상태 확인 중...</p>;
  }

  if (user) {
    return (
      <div className={compact ? "flex flex-col items-end gap-3" : "mt-8"}>
        <p className={compact ? "text-sm text-zinc-700" : "text-zinc-700"}>
          <strong>{user.nickname}</strong>님, 환영합니다.
        </p>
        <div
          className={
            compact
              ? "flex flex-wrap justify-end gap-3"
              : "mt-5 flex flex-wrap gap-3"
          }
        >
          {showInterviewCta && (
            <Link
              href="/interviews/new"
              className="rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
            >
              {interviewCtaLabel}
            </Link>
          )}
          {showDashboardCta && (
            <Link
              href="/dashboard"
              className="rounded-lg border border-zinc-300 px-5 py-2.5 text-sm font-medium hover:bg-zinc-50"
            >
              대시보드
            </Link>
          )}
          <button
            type="button"
            onClick={handleLogout}
            disabled={isLoading}
            className="rounded-lg border border-zinc-300 px-5 py-2.5 text-sm font-medium hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isLoading ? "로그아웃 중..." : "로그아웃"}
          </button>
        </div>
        {errorMessage && (
          <p
            role="alert"
            className={
              compact
                ? "basis-full text-right text-sm text-red-600"
                : "mt-3 text-sm text-red-600"
            }
          >
            {errorMessage}
          </p>
        )}
      </div>
    );
  }

  return (
    <div className="mt-8 flex justify-center gap-3">
      <Link
        href="/login"
        className="rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
      >
        로그인
      </Link>
      <Link
        href="/signup"
        className="rounded-lg border border-zinc-300 px-5 py-2.5 text-sm font-medium hover:bg-zinc-50"
      >
        회원가입
      </Link>
    </div>
  );
}
