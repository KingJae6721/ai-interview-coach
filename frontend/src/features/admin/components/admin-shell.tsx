"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";

import { useAuth } from "@/features/auth/context/auth-context";

const NAV_ITEMS = [
  { href: "/admin", label: "Dashboard" },
  { href: "/admin/users", label: "Users" },
  { href: "/admin/interviews", label: "Interviews" },
  { href: "/admin/audit-logs", label: "Audit Logs" },
];

export function AdminShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, isInitialized, logout } = useAuth();

  useEffect(() => {
    if (!isInitialized) return;
    if (!user) router.replace("/login?reason=auth-required");
    else if (user.role !== "ADMIN") router.replace("/dashboard");
  }, [isInitialized, router, user]);

  if (!isInitialized || !user || user.role !== "ADMIN") {
    return (
      <main className="flex min-h-screen items-center justify-center bg-slate-50">
        <p className="text-sm text-slate-500">
          관리자 권한을 확인하는 중입니다.
        </p>
      </main>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-950 lg:grid lg:grid-cols-[240px_1fr]">
      <aside className="border-b bg-slate-950 px-5 py-5 text-white lg:min-h-screen lg:border-r lg:border-b-0 lg:border-slate-800">
        <div className="flex items-center justify-between lg:block">
          <div>
            <p className="text-xs font-semibold tracking-[0.18em] text-slate-400 uppercase">
              AI Interview Coach
            </p>
            <p className="mt-1 text-lg font-semibold">Back Office</p>
          </div>
          <button
            type="button"
            onClick={() => void logout().then(() => router.replace("/login"))}
            className="rounded-md border border-slate-700 px-3 py-1.5 text-xs text-slate-300 hover:bg-slate-800 lg:hidden"
          >
            로그아웃
          </button>
        </div>
        <nav
          className="mt-5 flex gap-2 overflow-x-auto lg:flex-col"
          aria-label="관리자 메뉴"
        >
          {NAV_ITEMS.map((item) => {
            const active =
              item.href === "/admin"
                ? pathname === item.href
                : pathname.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`rounded-lg px-3 py-2.5 text-sm whitespace-nowrap ${active ? "bg-white text-slate-950" : "text-slate-300 hover:bg-slate-800 hover:text-white"}`}
              >
                {item.label}
              </Link>
            );
          })}
        </nav>
        <div className="mt-8 hidden border-t border-slate-800 pt-5 lg:block">
          <p className="truncate text-sm font-medium">{user.email}</p>
          <p className="mt-1 text-xs text-slate-400">ADMIN</p>
          <button
            type="button"
            onClick={() => void logout().then(() => router.replace("/login"))}
            className="mt-4 w-full rounded-lg border border-slate-700 px-3 py-2 text-sm text-slate-300 hover:bg-slate-800"
          >
            로그아웃
          </button>
        </div>
      </aside>
      <main className="min-w-0 px-4 py-8 sm:px-8 lg:px-10">{children}</main>
    </div>
  );
}
