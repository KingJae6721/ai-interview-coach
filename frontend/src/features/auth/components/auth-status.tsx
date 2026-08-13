"use client";

import Link from "next/link";
import { useState } from "react";

import { useAuth } from "@/features/auth/context/auth-context";
import { getErrorMessage } from "@/features/auth/lib/get-error-message";

export function AuthStatus() {
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
      <div className="mt-8">
        <p className="text-zinc-700">
          <strong>{user.nickname}</strong>님, 환영합니다.
        </p>
        <button
          type="button"
          onClick={handleLogout}
          disabled={isLoading}
          className="mt-5 rounded-lg border border-zinc-300 px-5 py-2.5 text-sm font-medium hover:bg-zinc-50 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isLoading ? "로그아웃 중..." : "로그아웃"}
        </button>
        {errorMessage && (
          <p role="alert" className="mt-3 text-sm text-red-600">
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
