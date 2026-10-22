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
import { RoleChangeButton } from "@/features/admin/components/role-change-button";
import { getAdminUsers } from "@/features/admin/services/admin-service";
import type {
  AdminUserListItem,
  PageResponse,
  UserStatus,
} from "@/features/admin/types/admin";
import { getErrorMessage } from "@/features/auth/lib/get-error-message";
import type { UserRole } from "@/features/auth/types/auth";

const PAGE_SIZE = 20;
export function AdminUsers() {
  const [emailInput, setEmailInput] = useState("");
  const [email, setEmail] = useState("");
  const [role, setRole] = useState<UserRole | "">("");
  const [status, setStatus] = useState<UserStatus | "">("");
  const [pageNumber, setPageNumber] = useState(0);
  const [data, setData] = useState<PageResponse<AdminUserListItem> | null>(
    null,
  );
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  useEffect(() => {
    let active = true;
    getAdminUsers({
      email: email || undefined,
      role: role || undefined,
      status: status || undefined,
      page: pageNumber,
      size: PAGE_SIZE,
    })
      .then((v) => active && setData(v))
      .catch((e: unknown) => active && setError(getErrorMessage(e)));
    return () => {
      active = false;
    };
  }, [email, pageNumber, reload, role, status]);
  function refresh() {
    setError("");
    setData(null);
    setReload((v) => v + 1);
  }
  return (
    <section>
      <header>
        <p className="text-sm text-slate-500">계정과 권한 관리</p>
        <h1 className="mt-1 text-3xl font-semibold">Users</h1>
      </header>
      <form
        onSubmit={(event) => {
          event.preventDefault();
          setPageNumber(0);
          setEmail(emailInput.trim());
        }}
        className="mt-7 grid gap-3 rounded-xl border bg-white p-4 md:grid-cols-[1fr_160px_160px_auto]"
      >
        <input
          value={emailInput}
          onChange={(e) => setEmailInput(e.target.value)}
          placeholder="이메일 검색"
          className="rounded-lg border px-3 py-2 text-sm"
        />
        <select
          value={role}
          onChange={(e) => {
            setPageNumber(0);
            setRole(e.target.value as UserRole | "");
          }}
          className="rounded-lg border px-3 py-2 text-sm"
        >
          <option value="">전체 Role</option>
          <option value="USER">USER</option>
          <option value="ADMIN">ADMIN</option>
        </select>
        <select
          value={status}
          onChange={(e) => {
            setPageNumber(0);
            setStatus(e.target.value as UserStatus | "");
          }}
          className="rounded-lg border px-3 py-2 text-sm"
        >
          <option value="">전체 상태</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="DELETED">DELETED</option>
        </select>
        <button className="rounded-lg bg-slate-950 px-4 py-2 text-sm text-white">
          검색
        </button>
      </form>
      <div className="mt-5">
        {error ? (
          <AdminError message={error} retry={refresh} />
        ) : !data ? (
          <AdminLoading />
        ) : data.empty ? (
          <AdminEmpty>조건에 맞는 사용자가 없습니다.</AdminEmpty>
        ) : (
          <>
            <div className="overflow-x-auto rounded-xl border bg-white">
              <table className="min-w-full text-left text-sm">
                <thead className="border-b bg-slate-50 text-xs text-slate-500 uppercase">
                  <tr>
                    <th className="px-4 py-3">ID</th>
                    <th className="px-4 py-3">Email</th>
                    <th className="px-4 py-3">Role</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3">Interviews</th>
                    <th className="px-4 py-3">Created At</th>
                    <th className="px-4 py-3">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((user) => (
                    <tr key={user.userId} className="border-b last:border-0">
                      <td className="px-4 py-3">#{user.userId}</td>
                      <td className="px-4 py-3">{user.email}</td>
                      <td className="px-4 py-3">
                        <span
                          className={`rounded-full px-2 py-1 text-xs font-semibold ${user.role === "ADMIN" ? "bg-violet-100 text-violet-700" : "bg-slate-100 text-slate-700"}`}
                        >
                          {user.role}
                        </span>
                      </td>
                      <td className="px-4 py-3">{user.status}</td>
                      <td className="px-4 py-3">{user.interviewCount}</td>
                      <td className="px-4 py-3 text-slate-500">
                        {formatAdminDate(user.createdAt)}
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex gap-2">
                          <Link
                            href={`/admin/users/${user.userId}`}
                            className="rounded-md border px-3 py-1.5 text-xs font-medium hover:bg-slate-50"
                          >
                            상세
                          </Link>
                          <RoleChangeButton
                            userId={user.userId}
                            email={user.email}
                            role={user.role}
                            onChanged={(next) =>
                              setData((current) =>
                                current
                                  ? {
                                      ...current,
                                      content: current.content.map((item) =>
                                        item.userId === user.userId
                                          ? { ...item, role: next }
                                          : item,
                                      ),
                                    }
                                  : current,
                              )
                            }
                          />
                        </div>
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
