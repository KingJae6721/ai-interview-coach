import { AdminUserDetail } from "@/features/admin/components/admin-user-detail";

export default async function AdminUserDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <AdminUserDetail userId={Number(id)} />;
}
