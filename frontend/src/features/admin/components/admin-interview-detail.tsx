"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import {
  AdminError,
  AdminLoading,
  formatAdminDate,
  StatusBadge,
} from "@/features/admin/components/admin-common";
import { getAdminInterview } from "@/features/admin/services/admin-service";
import type { AdminInterviewDetail as Detail } from "@/features/admin/types/admin";
import { getErrorMessage } from "@/features/auth/lib/get-error-message";

export function AdminInterviewDetail({ interviewId }: { interviewId: number }) {
  const [data, setData] = useState<Detail | null>(null);
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  useEffect(() => {
    let active = true;
    getAdminInterview(interviewId)
      .then((v) => active && setData(v))
      .catch((e: unknown) => active && setError(getErrorMessage(e)));
    return () => {
      active = false;
    };
  }, [interviewId, reload]);
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
  return (
    <section>
      <Link href="/admin/interviews" className="text-sm text-blue-700">
        ← Interviews
      </Link>
      <div className="mt-4 flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm text-slate-500">
            Interview #{data.interviewId}
          </p>
          <h1 className="mt-1 text-3xl font-semibold">{data.title}</h1>
        </div>
        <StatusBadge status={data.status} />
      </div>
      <div className="mt-7 grid gap-5 lg:grid-cols-3">
        <article className="rounded-xl border bg-white p-5">
          <h2 className="font-semibold">Interview</h2>
          <dl className="mt-4 space-y-3 text-sm">
            <div>
              <dt className="text-slate-500">생성</dt>
              <dd>{formatAdminDate(data.createdAt)}</dd>
            </div>
            <div>
              <dt className="text-slate-500">시작</dt>
              <dd>{formatAdminDate(data.startedAt)}</dd>
            </div>
            <div>
              <dt className="text-slate-500">완료</dt>
              <dd>{formatAdminDate(data.completedAt)}</dd>
            </div>
            <div>
              <dt className="text-slate-500">취소</dt>
              <dd>{formatAdminDate(data.cancelledAt)}</dd>
            </div>
          </dl>
        </article>
        <article className="rounded-xl border bg-white p-5">
          <h2 className="font-semibold">User</h2>
          <p className="mt-4 text-sm">
            <Link
              href={`/admin/users/${data.userId}`}
              className="text-blue-700"
            >
              #{data.userId} {data.userEmail}
            </Link>
          </p>
          <p className="mt-5 text-sm text-slate-500">Resume 사용 여부</p>
          <p className="mt-1 text-sm font-medium">
            {data.resumeUsed ? `사용 (ID #${data.resumeId})` : "사용 안 함"}
          </p>
        </article>
        <article className="rounded-xl border bg-white p-5">
          <h2 className="font-semibold">Job Posting</h2>
          <p className="mt-4 text-sm font-medium">
            {data.jobPostingTitle ?? "-"}
          </p>
          <p className="mt-2 text-sm leading-6 text-slate-500">
            {data.jobPostingSummary ?? "공고 요약이 없습니다."}
          </p>
          {data.jobPostingId && (
            <p className="mt-3 text-xs text-slate-400">
              ID #{data.jobPostingId}
            </p>
          )}
        </article>
      </div>
      <h2 className="mt-8 text-lg font-semibold">Questions</h2>
      <div className="mt-3 space-y-3">
        {data.questions.length === 0 ? (
          <p className="rounded-xl border bg-white p-8 text-sm text-slate-500">
            질문이 없습니다.
          </p>
        ) : (
          data.questions.map((q) => (
            <article
              key={q.questionId}
              className="rounded-xl border bg-white p-5"
            >
              <div className="flex flex-wrap items-center gap-2 text-xs">
                <span className="font-semibold text-slate-500">
                  #{q.questionOrder}
                </span>
                <span className="rounded bg-slate-100 px-2 py-1">{q.type}</span>
                <span className="rounded bg-blue-50 px-2 py-1 text-blue-700">
                  {q.category}
                </span>
                <span className="rounded bg-violet-50 px-2 py-1 text-violet-700">
                  {q.difficulty}
                </span>
                {q.parentQuestionId && (
                  <span className="text-slate-500">
                    parent #{q.parentQuestionId}
                  </span>
                )}
              </div>
              <p className="mt-3 text-sm leading-6">{q.questionContent}</p>
              <div className="mt-4 flex gap-4 border-t pt-3 text-xs text-slate-500">
                <span>답변 {q.answerExists ? "있음" : "없음"}</span>
                <span>평가 {q.evaluationExists ? "있음" : "없음"}</span>
              </div>
            </article>
          ))
        )}
      </div>
    </section>
  );
}
