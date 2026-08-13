"use client";

import { redirect } from "next/navigation";
import type { ReactNode } from "react";

import { useAuth } from "@/features/auth/context/auth-context";

function AuthLoading() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-zinc-50">
      <p className="text-sm text-zinc-500">인증 상태 확인 중...</p>
    </div>
  );
}

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, isInitialized } = useAuth();

  if (!isInitialized) {
    return <AuthLoading />;
  }

  if (!user) {
    redirect("/login?reason=auth-required");
  }

  return children;
}

export function PublicOnlyRoute({ children }: { children: ReactNode }) {
  const { user, isInitialized } = useAuth();

  if (!isInitialized) {
    return <AuthLoading />;
  }

  if (user) {
    redirect("/dashboard");
  }

  return children;
}
