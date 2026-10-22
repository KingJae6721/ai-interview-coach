"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import {
  AdminEmpty,
  AdminError,
  AdminLoading,
  AdminPagination,
  formatAdminDate,
} from "@/features/admin/components/admin-common";
import { getAdminAuditLogs } from "@/features/admin/services/admin-service";
import type { AdminAuditLog, PageResponse } from "@/features/admin/types/admin";
import { getErrorMessage } from "@/features/auth/lib/get-error-message";

export function AdminAuditLogs() {
  const [actorInput, setActorInput] = useState("");
  const [actorUserId, setActorUserId] = useState<number | undefined>();
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [pageNumber, setPageNumber] = useState(0);
  const [data, setData] = useState<PageResponse<AdminAuditLog> | null>(null);
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  useEffect(() => {
    let active = true;
    getAdminAuditLogs({
      action: "USER_ROLE_CHANGED",
      targetType: "USER",
      actorUserId,
      fromDate: fromDate || undefined,
      toDate: toDate || undefined,
      page: pageNumber,
      size: 20,
    })
      .then((v) => active && setData(v))
      .catch((e: unknown) => active && setError(getErrorMessage(e)));
    return () => {
      active = false;
    };
  }, [actorUserId, fromDate, pageNumber, reload, toDate]);
  return (
    <section>
      <header>
        <p className="text-sm text-slate-500">관리자 변경 이력 · 읽기 전용</p>
        <h1 className="mt-1 text-3xl font-semibold">Audit Logs</h1>
      </header>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          const value = actorInput.trim();
          setPageNumber(0);
          setActorUserId(value ? Number(value) : undefined);
        }}
        className="mt-7 grid gap-3 rounded-xl border bg-white p-4 md:grid-cols-[1fr_170px_170px_auto]"
      >
        <input
          type="number"
          min="1"
          value={actorInput}
          onChange={(e) => setActorInput(e.target.value)}
          placeholder="Actor User ID"
          className="rounded-lg border px-3 py-2 text-sm"
        />
        <input
          aria-label="시작일"
          type="date"
          value={fromDate}
          onChange={(e) => {
            setPageNumber(0);
            setFromDate(e.target.value);
          }}
          className="rounded-lg border px-3 py-2 text-sm"
        />
        <input
          aria-label="종료일"
          type="date"
          value={toDate}
          onChange={(e) => {
            setPageNumber(0);
            setToDate(e.target.value);
          }}
          className="rounded-lg border px-3 py-2 text-sm"
        />
        <button className="rounded-lg bg-slate-950 px-4 py-2 text-sm text-white">
          조회
        </button>
      </form>
      <div className="mt-5">
        {error ? (
          <AdminError
            message={error}
            retry={() => {
              setError("");
              setReload((v) => v + 1);
            }}
          />
        ) : !data ? (
          <AdminLoading />
        ) : data.empty ? (
          <AdminEmpty>감사 로그가 없습니다.</AdminEmpty>
        ) : (
          <>
            <div className="overflow-x-auto rounded-xl border bg-white">
              <table className="min-w-full text-left text-sm">
                <thead className="border-b bg-slate-50 text-xs text-slate-500 uppercase">
                  <tr>
                    <th className="px-4 py-3">Timestamp</th>
                    <th className="px-4 py-3">Actor</th>
                    <th className="px-4 py-3">Action</th>
                    <th className="px-4 py-3">Target</th>
                    <th className="px-4 py-3">Before</th>
                    <th className="px-4 py-3">After</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((log) => (
                    <tr key={log.auditLogId} className="border-b last:border-0">
                      <td className="px-4 py-3 whitespace-nowrap text-slate-500">
                        {formatAdminDate(log.createdAt)}
                      </td>
                      <td className="px-4 py-3">
                        <Link
                          href={`/admin/users/${log.actorUserId}`}
                          className="text-blue-700"
                        >
                          #{log.actorUserId}
                        </Link>
                      </td>
                      <td className="px-4 py-3 font-medium">{log.action}</td>
                      <td className="px-4 py-3">
                        <Link
                          href={`/admin/users/${log.targetId}`}
                          className="text-blue-700"
                        >
                          {log.targetType} #{log.targetId}
                        </Link>
                      </td>
                      <td className="px-4 py-3">{log.beforeValue}</td>
                      <td className="px-4 py-3">{log.afterValue}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <AdminPagination page={data} onChange={setPageNumber} />
          </>
        )}
      </div>
    </section>
  );
}
