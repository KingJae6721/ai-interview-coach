"use client";

import { useState } from "react";

import { updateAdminUserRole } from "@/features/admin/services/admin-service";
import type { UserRole } from "@/features/auth/types/auth";
import { getErrorMessage } from "@/features/auth/lib/get-error-message";

export function RoleChangeButton({
  userId,
  email,
  role,
  onChanged,
}: {
  userId: number;
  email: string;
  role: UserRole;
  onChanged: (role: UserRole) => void;
}) {
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const nextRole: UserRole = role === "ADMIN" ? "USER" : "ADMIN";

  async function submit() {
    setSaving(true);
    setError("");
    try {
      const result = await updateAdminUserRole(userId, nextRole);
      onChanged(result.role);
      setOpen(false);
    } catch (reason) {
      setError(getErrorMessage(reason));
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        className="rounded-md border px-3 py-1.5 text-xs font-medium hover:bg-slate-50"
      >
        Role 변경
      </button>
      {open && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby={`role-title-${userId}`}
        >
          <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl">
            <h2 id={`role-title-${userId}`} className="text-lg font-semibold">
              관리자 권한 변경
            </h2>
            <dl className="mt-5 grid grid-cols-[90px_1fr] gap-2 text-sm">
              <dt className="text-slate-500">대상</dt>
              <dd className="font-medium break-all">{email}</dd>
              <dt className="text-slate-500">현재 Role</dt>
              <dd>{role}</dd>
              <dt className="text-slate-500">변경 Role</dt>
              <dd className="font-semibold text-blue-700">{nextRole}</dd>
            </dl>
            <p className="mt-5 rounded-lg bg-amber-50 p-3 text-sm text-amber-900">
              이 사용자의 관리자 권한을 변경하시겠습니까?
            </p>
            {error && (
              <p role="alert" className="mt-3 text-sm text-red-700">
                {error}
              </p>
            )}
            <div className="mt-6 flex justify-end gap-2">
              <button
                type="button"
                disabled={saving}
                onClick={() => setOpen(false)}
                className="rounded-lg border px-4 py-2 text-sm"
              >
                취소
              </button>
              <button
                type="button"
                disabled={saving}
                onClick={() => void submit()}
                className="rounded-lg bg-slate-950 px-4 py-2 text-sm text-white disabled:opacity-50"
              >
                {saving ? "변경 중..." : "확인"}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
