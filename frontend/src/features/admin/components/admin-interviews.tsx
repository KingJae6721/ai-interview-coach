"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import {
  AdminEmpty,
  AdminError,
  AdminLoading,
  AdminPagination,
  formatAdminDate,
  StatusBadge,
} from "@/features/admin/components/admin-common";
import { getAdminInterviews } from "@/features/admin/services/admin-service";
import type {
  AdminInterviewListItem,
  InterviewStatus,
  PageResponse,
} from "@/features/admin/types/admin";
import { getErrorMessage } from "@/features/auth/lib/get-error-message";

export function AdminInterviews() {
  const [emailInput, setEmailInput] = useState("");
  const [email, setEmail] = useState("");
  const [status, setStatus] = useState<InterviewStatus | "">("");
  const [pageNumber, setPageNumber] = useState(0);
  const [data, setData] = useState<PageResponse<AdminInterviewListItem> | null>(
    null,
  );
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  useEffect(() => {
    let active = true;
    getAdminInterviews({
      userEmail: email || undefined,
      status: status || undefined,
      page: pageNumber,
      size: 20,
    })
      .then((v) => active && setData(v))
      .catch((e: unknown) => active && setError(getErrorMessage(e)));
    return () => {
      active = false;
    };
  }, [email, pageNumber, reload, status]);
  return (
    <section>
      <header>
        <p className="text-sm text-slate-500">면접 운영 상태</p>
        <h1 className="mt-1 text-3xl font-semibold">Interviews</h1>
      </header>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          setPageNumber(0);
          setEmail(emailInput.trim());
        }}
        className="mt-7 grid gap-3 rounded-xl border bg-white p-4 md:grid-cols-[1fr_200px_auto]"
      >
        <input
          value={emailInput}
          onChange={(e) => setEmailInput(e.target.value)}
          placeholder="사용자 이메일 검색"
          className="rounded-lg border px-3 py-2 text-sm"
        />
        <select
          value={status}
          onChange={(e) => {
            setPageNumber(0);
            setStatus(e.target.value as InterviewStatus | "");
          }}
          className="rounded-lg border px-3 py-2 text-sm"
        >
          <option value="">전체 상태</option>
          {["READY", "IN_PROGRESS", "COMPLETED", "CANCELLED"].map((v) => (
            <option key={v} value={v}>
              {v}
            </option>
          ))}
        </select>
        <button className="rounded-lg bg-slate-950 px-4 py-2 text-sm text-white">
          검색
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
          <AdminEmpty>조건에 맞는 면접이 없습니다.</AdminEmpty>
        ) : (
          <>
            <div className="overflow-x-auto rounded-xl border bg-white">
              <table className="min-w-full text-left text-sm">
                <thead className="border-b bg-slate-50 text-xs text-slate-500 uppercase">
                  <tr>
                    <th className="px-4 py-3">ID</th>
                    <th className="px-4 py-3">Title</th>
                    <th className="px-4 py-3">User</th>
                    <th className="px-4 py-3">Posting Summary</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3">Questions</th>
                    <th className="px-4 py-3">Created At</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((item) => (
                    <tr
                      key={item.interviewId}
                      className="border-b last:border-0"
                    >
                      <td className="px-4 py-3">
                        <Link
                          href={`/admin/interviews/${item.interviewId}`}
                          className="font-medium text-blue-700"
                        >
                          #{item.interviewId}
                        </Link>
                      </td>
                      <td className="max-w-52 px-4 py-3 font-medium">
                        {item.title}
                      </td>
                      <td className="px-4 py-3">
                        <Link
                          href={`/admin/users/${item.userId}`}
                          className="text-blue-700"
                        >
                          {item.userEmail}
                        </Link>
                      </td>
                      <td className="max-w-64 truncate px-4 py-3 text-slate-500">
                        {item.jobPostingSummary ?? "-"}
                      </td>
                      <td className="px-4 py-3">
                        <StatusBadge status={item.status} />
                      </td>
                      <td className="px-4 py-3">
                        {item.answeredQuestionCount} / {item.questionCount}
                      </td>
                      <td className="px-4 py-3 text-slate-500">
                        {formatAdminDate(item.createdAt)}
                      </td>
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
