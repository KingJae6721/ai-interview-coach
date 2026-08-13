import Link from "next/link";

import { AuthStatus } from "@/features/auth/components/auth-status";
import { ProtectedRoute } from "@/features/auth/components/route-guards";

export default function DashboardPage() {
  return (
    <ProtectedRoute>
      <main className="min-h-screen bg-zinc-50 px-6 py-10">
        <div className="mx-auto max-w-5xl">
          <header className="flex flex-col gap-4 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm sm:flex-row sm:items-center sm:justify-between">
            <div>
              <Link
                href="/"
                className="text-sm text-zinc-500 hover:text-zinc-900"
              >
                AI Interview Coach
              </Link>
              <h1 className="mt-2 text-2xl font-semibold tracking-tight">
                대시보드
              </h1>
            </div>
            <AuthStatus />
          </header>

          <section className="mt-6 rounded-2xl border border-dashed border-zinc-300 bg-white p-10 text-center">
            <h2 className="text-lg font-medium">면접 분석 대시보드</h2>
            <p className="mt-2 text-sm text-zinc-500">
              다음 Sprint에서 면접 통계와 최근 기록을 연결합니다.
            </p>
            <Link
              href="/interviews/new"
              className="mt-6 inline-block rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
            >
              새 면접 만들기
            </Link>
          </section>
        </div>
      </main>
    </ProtectedRoute>
  );
}
