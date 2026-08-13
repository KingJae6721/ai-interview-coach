import Link from "next/link";

import { ProtectedRoute } from "@/features/auth/components/route-guards";

interface InterviewPageProps {
  params: Promise<{ interviewId: string }>;
}

export default async function InterviewPage({ params }: InterviewPageProps) {
  const { interviewId } = await params;

  return (
    <ProtectedRoute>
      <main className="flex min-h-screen items-center justify-center bg-zinc-50 px-6">
        <section className="w-full max-w-xl rounded-2xl border border-zinc-200 bg-white p-8 text-center shadow-sm">
          <p className="text-sm font-medium text-emerald-700">면접 생성 완료</p>
          <h1 className="mt-3 text-2xl font-semibold">면접 #{interviewId}</h1>
          <p className="mt-3 text-sm text-zinc-600">
            다음 Sprint에서 질문 조회와 면접 진행 화면을 연결합니다.
          </p>
          <Link
            href="/dashboard"
            className="mt-7 inline-block rounded-lg bg-zinc-900 px-5 py-2.5 text-sm font-medium text-white hover:bg-zinc-700"
          >
            대시보드로 이동
          </Link>
        </section>
      </main>
    </ProtectedRoute>
  );
}
