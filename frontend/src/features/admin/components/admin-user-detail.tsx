"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import {
  AdminError,
  AdminLoading,
  formatAdminDate,
  StatusBadge,
} from "@/features/admin/components/admin-common";
import { RoleChangeButton } from "@/features/admin/components/role-change-button";
import { getAdminUser } from "@/features/admin/services/admin-service";
import type { AdminUserDetail as Detail } from "@/features/admin/types/admin";
import { getErrorMessage } from "@/features/auth/lib/get-error-message";

export function AdminUserDetail({ userId }: { userId: number }) {
  const [data, setData] = useState<Detail | null>(null);
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  useEffect(() => {
    let active = true;
    getAdminUser(userId)
      .then((v) => active && setData(v))
      .catch((e: unknown) => active && setError(getErrorMessage(e)));
    return () => {
      active = false;
    };
  }, [reload, userId]);
  if (error)
    return (
      <AdminError
        message={error}
        retry={() => {
          setError("");
          setData(null);
          setReload((v) => v + 1);
        }}
      />
    );
  if (!data) return <AdminLoading />;
  const stats = data.interviewStatistics;
  return (
    <section>
      <Link href="/admin/users" className="text-sm text-blue-700">
        ← Users
      </Link>
      <div className="mt-4 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-sm text-slate-500">User #{data.userId}</p>
          <h1 className="mt-1 text-3xl font-semibold break-all">
            {data.email}
          </h1>
        </div>
        <RoleChangeButton
          userId={data.userId}
          email={data.email}
          role={data.role}
          onChanged={(role) => setData({ ...data, role })}
        />
      </div>
      <div className="mt-7 grid gap-5 lg:grid-cols-2">
        <article className="rounded-xl border bg-white p-6">
          <h2 className="font-semibold">계정 정보</h2>
          <dl className="mt-4 grid grid-cols-[110px_1fr] gap-3 text-sm">
            <dt className="text-slate-500">닉네임</dt>
            <dd>{data.nickname}</dd>
            <dt className="text-slate-500">Role</dt>
            <dd>{data.role}</dd>
            <dt className="text-slate-500">Status</dt>
            <dd>{data.status}</dd>
            <dt className="text-slate-500">가입일</dt>
            <dd>{formatAdminDate(data.createdAt)}</dd>
          </dl>
        </article>
        <article className="rounded-xl border bg-white p-6">
          <h2 className="font-semibold">면접 통계</h2>
          <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
            <p>
              전체 <b>{stats.totalInterviews}</b>
            </p>
            <p>
              준비 <b>{stats.readyInterviews}</b>
            </p>
            <p>
              진행 <b>{stats.inProgressInterviews}</b>
            </p>
            <p>
              완료 <b>{stats.completedInterviews}</b>
            </p>
            <p>
              취소 <b>{stats.cancelledInterviews}</b>
            </p>
          </div>
        </article>
      </div>
      <article className="mt-5 rounded-xl border bg-white p-6">
        <h2 className="font-semibold">최근 면접</h2>
        {data.latestInterview ? (
          <div className="mt-4 flex flex-wrap items-center gap-4 text-sm">
            <Link
              href={`/admin/interviews/${data.latestInterview.interviewId}`}
              className="font-medium text-blue-700"
            >
              #{data.latestInterview.interviewId} {data.latestInterview.title}
            </Link>
            <StatusBadge status={data.latestInterview.status} />
            <span className="text-slate-500">
              {formatAdminDate(data.latestInterview.createdAt)}
            </span>
          </div>
        ) : (
          <p className="mt-4 text-sm text-slate-500">면접 이력이 없습니다.</p>
        )}
      </article>
    </section>
  );
}
