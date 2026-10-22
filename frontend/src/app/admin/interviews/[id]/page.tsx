import { AdminInterviewDetail } from "@/features/admin/components/admin-interview-detail";

export default async function AdminInterviewDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <AdminInterviewDetail interviewId={Number(id)} />;
}
