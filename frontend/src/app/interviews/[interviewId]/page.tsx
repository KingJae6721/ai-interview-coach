import { notFound } from "next/navigation";

import { ProtectedRoute } from "@/features/auth/components/route-guards";
import { InterviewProgress } from "@/features/interview/components/interview-progress";

interface InterviewPageProps {
  params: Promise<{ interviewId: string }>;
}

export default async function InterviewPage({ params }: InterviewPageProps) {
  const { interviewId: interviewIdParam } = await params;
  const interviewId = Number(interviewIdParam);

  if (!Number.isSafeInteger(interviewId) || interviewId <= 0) {
    notFound();
  }

  return (
    <ProtectedRoute>
      <main className="min-h-screen bg-zinc-50 px-5 py-8 sm:px-6 sm:py-10">
        <div className="mx-auto max-w-3xl">
          <InterviewProgress interviewId={interviewId} />
        </div>
      </main>
    </ProtectedRoute>
  );
}
