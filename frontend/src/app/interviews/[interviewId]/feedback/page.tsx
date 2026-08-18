import { redirect } from "next/navigation";

interface FeedbackPageProps {
  params: Promise<{ interviewId: string }>;
}

export default async function FeedbackPage({ params }: FeedbackPageProps) {
  const { interviewId } = await params;
  redirect(`/interviews/${interviewId}/result`);
}
