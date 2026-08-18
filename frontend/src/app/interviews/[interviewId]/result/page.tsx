import { notFound } from "next/navigation";

import { ProtectedRoute } from "@/features/auth/components/route-guards";
import { InterviewResult } from "@/features/interview/components/interview-result";

interface InterviewResultPageProps {
  params: Promise<{ interviewId: string }>;
}

export default async function InterviewResultPage({
  params,
}: InterviewResultPageProps) {
  const { interviewId: interviewIdParam } = await params;
  const interviewId = Number(interviewIdParam);

  if (!Number.isSafeInteger(interviewId) || interviewId <= 0) {
    notFound();
  }

  return (
    <ProtectedRoute>
      <main className="min-h-screen bg-zinc-50 px-5 py-8 sm:px-6 sm:py-10">
        <div className="mx-auto max-w-5xl">
          <InterviewResult interviewId={interviewId} />
        </div>
      </main>
    </ProtectedRoute>
  );
}
