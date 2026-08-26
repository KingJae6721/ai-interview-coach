import Link from "next/link";

import { AuthStatus } from "@/features/auth/components/auth-status";
import { ProtectedRoute } from "@/features/auth/components/route-guards";
import { InterviewHistory } from "@/features/interview/components/interview-history";

export default function InterviewHistoryPage() {
  return (
    <ProtectedRoute>
      <main className="min-h-screen bg-zinc-50 px-5 py-8 sm:px-6 sm:py-10">
        <div className="mx-auto max-w-5xl">
          <header className="flex flex-col gap-4 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm sm:flex-row sm:items-center sm:justify-between">
            <div>
              <Link
                href="/dashboard"
                className="text-sm text-zinc-500 hover:text-zinc-900"
              >
                ← 대시보드
              </Link>
              <h1 className="mt-2 text-2xl font-semibold tracking-tight text-zinc-900">
                면접 이력
              </h1>
              <p className="mt-1 text-sm text-zinc-500">
                지난 면접의 진행 상태와 결과를 확인하세요.
              </p>
            </div>
            <div className="flex items-center gap-3">
              <Link
                href="/interviews/new"
                className="rounded-lg bg-zinc-900 px-4 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
              >
                새 면접 만들기
              </Link>
              <AuthStatus />
            </div>
          </header>

          <section className="mt-6">
            <InterviewHistory />
          </section>
        </div>
      </main>
    </ProtectedRoute>
  );
}
