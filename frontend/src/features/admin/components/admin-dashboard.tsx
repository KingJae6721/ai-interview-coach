"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import {
  AdminEmpty,
  AdminError,
  AdminLoading,
  formatAdminDate,
  StatusBadge,
} from "@/features/admin/components/admin-common";
import { getAdminDashboard } from "@/features/admin/services/admin-service";
import type { AdminDashboard as AdminDashboardData } from "@/features/admin/types/admin";
import { getErrorMessage } from "@/features/auth/lib/get-error-message";

export function AdminDashboard() {
  const [data, setData] = useState<AdminDashboardData | null>(null);
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);

  useEffect(() => {
    let active = true;
    getAdminDashboard()
      .then((value) => active && setData(value))
      .catch((reason: unknown) => active && setError(getErrorMessage(reason)));
    return () => {
      active = false;
    };
  }, [reload]);

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
  if (!data) return <AdminLoading label="관리자 통계를 불러오는 중입니다." />;

  const cards = [
    ["전체 사용자", data.totalUsers],
    ["전체 면접", data.totalInterviews],
    ["진행 중", data.inProgressInterviews],
    ["완료", data.completedInterviews],
    ["취소", data.cancelledInterviews],
    ["오늘 생성", data.todayCreatedInterviews],
  ];
  return (
    <section>
      <header>
        <p className="text-sm text-slate-500">운영 현황</p>
        <h1 className="mt-1 text-3xl font-semibold tracking-tight">
          Dashboard
        </h1>
      </header>
      <div className="mt-7 grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {cards.map(([label, value]) => (
          <article key={label} className="rounded-xl border bg-white p-5">
            <p className="text-sm text-slate-500">{label}</p>
            <p className="mt-2 text-3xl font-semibold">{value}</p>
          </article>
        ))}
      </div>
      <div className="mt-8 flex items-center justify-between">
        <h2 className="text-lg font-semibold">최근 면접</h2>
        <Link
          href="/admin/interviews"
          className="text-sm font-medium text-blue-700"
        >
          전체 보기
        </Link>
      </div>
      <div className="mt-3 overflow-x-auto rounded-xl border bg-white">
        {data.recentInterviews.length === 0 ? (
          <AdminEmpty>최근 면접이 없습니다.</AdminEmpty>
        ) : (
          <table className="min-w-full text-left text-sm">
            <thead className="border-b bg-slate-50 text-xs text-slate-500 uppercase">
              <tr>
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">제목</th>
                <th className="px-4 py-3">사용자</th>
                <th className="px-4 py-3">상태</th>
                <th className="px-4 py-3">생성일</th>
              </tr>
            </thead>
            <tbody>
              {data.recentInterviews.map((item) => (
                <tr key={item.interviewId} className="border-b last:border-0">
                  <td className="px-4 py-3">
                    <Link
                      href={`/admin/interviews/${item.interviewId}`}
                      className="font-medium text-blue-700"
                    >
                      #{item.interviewId}
                    </Link>
                  </td>
                  <td className="px-4 py-3">{item.title}</td>
                  <td className="px-4 py-3">{item.userEmail}</td>
                  <td className="px-4 py-3">
                    <StatusBadge status={item.status} />
                  </td>
                  <td className="px-4 py-3 text-slate-500">
                    {formatAdminDate(item.createdAt)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}
