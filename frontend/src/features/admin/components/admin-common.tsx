"use client";

import type { ReactNode } from "react";

import type {
  InterviewStatus,
  PageResponse,
} from "@/features/admin/types/admin";

export function formatAdminDate(value: string | null): string {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? value
    : new Intl.DateTimeFormat("ko-KR", {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(date);
}

const STATUS_STYLE: Record<InterviewStatus, string> = {
  READY: "bg-slate-100 text-slate-700",
  IN_PROGRESS: "bg-blue-100 text-blue-700",
  COMPLETED: "bg-emerald-100 text-emerald-700",
  CANCELLED: "bg-amber-100 text-amber-800",
};

export function StatusBadge({ status }: { status: InterviewStatus }) {
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${STATUS_STYLE[status]}`}
    >
      {status}
    </span>
  );
}

export function AdminLoading({ label = "데이터를 불러오는 중입니다." }) {
  return (
    <div role="status" className="rounded-xl border bg-white p-10 text-center">
      <p className="text-sm text-slate-500">{label}</p>
    </div>
  );
}

export function AdminError({
  message,
  retry,
}: {
  message: string;
  retry: () => void;
}) {
  return (
    <div className="rounded-xl border border-red-200 bg-red-50 p-8 text-center">
      <p role="alert" className="text-sm text-red-700">
        {message}
      </p>
      <button
        type="button"
        onClick={retry}
        className="mt-4 rounded-lg bg-slate-900 px-4 py-2 text-sm text-white"
      >
        다시 시도
      </button>
    </div>
  );
}

export function AdminEmpty({ children }: { children: ReactNode }) {
  return (
    <div className="rounded-xl border border-dashed bg-white p-10 text-center text-sm text-slate-500">
      {children}
    </div>
  );
}

export function AdminPagination<T>({
  page,
  onChange,
}: {
  page: PageResponse<T>;
  onChange: (page: number) => void;
}) {
  return (
    <nav
      aria-label="페이지 이동"
      className="flex items-center justify-between pt-4"
    >
      <button
        type="button"
        disabled={page.first}
        onClick={() => onChange(page.number - 1)}
        className="rounded-lg border px-4 py-2 text-sm disabled:opacity-40"
      >
        이전
      </button>
      <span className="text-sm text-slate-500">
        {page.number + 1} / {Math.max(page.totalPages, 1)}
      </span>
      <button
        type="button"
        disabled={page.last}
        onClick={() => onChange(page.number + 1)}
        className="rounded-lg border px-4 py-2 text-sm disabled:opacity-40"
      >
        다음
      </button>
    </nav>
  );
}
