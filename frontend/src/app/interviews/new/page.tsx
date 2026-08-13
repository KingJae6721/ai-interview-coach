import Link from "next/link";

import { ProtectedRoute } from "@/features/auth/components/route-guards";
import { InterviewCreateForm } from "@/features/interview/components/interview-create-form";

export default function NewInterviewPage() {
  return (
    <ProtectedRoute>
      <main className="min-h-screen bg-zinc-50 px-5 py-10 sm:px-6">
        <section className="mx-auto w-full max-w-xl rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm sm:p-8">
          <Link
            href="/dashboard"
            className="text-sm text-zinc-500 hover:text-zinc-900"
          >
            ← 대시보드로
          </Link>
          <h1 className="mt-6 text-2xl font-semibold tracking-tight">
            새 면접 만들기
          </h1>
          <p className="mt-2 text-sm leading-6 text-zinc-600">
            연습할 직무와 면접 제목을 선택해 주세요.
          </p>

          <InterviewCreateForm />
        </section>
      </main>
    </ProtectedRoute>
  );
}
