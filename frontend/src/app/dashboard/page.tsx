import Link from "next/link";

import { AuthStatus } from "@/features/auth/components/auth-status";
import { ProtectedRoute } from "@/features/auth/components/route-guards";
import { DashboardInsights } from "@/features/dashboard/components/dashboard-insights";
import { DashboardSummary } from "@/features/dashboard/components/dashboard-summary";

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

          <DashboardSummary />
          <div className="mt-6 space-y-6">
            <DashboardInsights />
          </div>
        </div>
      </main>
    </ProtectedRoute>
  );
}
