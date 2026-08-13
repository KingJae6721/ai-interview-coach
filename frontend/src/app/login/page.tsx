import Link from "next/link";

import { LoginForm } from "@/features/auth/components/login-form";
import { PublicOnlyRoute } from "@/features/auth/components/route-guards";

interface LoginPageProps {
  searchParams: Promise<{
    signup?: string | string[];
    reason?: string | string[];
  }>;
}

export default async function LoginPage({ searchParams }: LoginPageProps) {
  const { signup, reason } = await searchParams;
  const initialMessage =
    signup === "success"
      ? "회원가입이 완료되었습니다. 로그인해 주세요."
      : reason === "session-expired"
        ? "인증이 만료되었습니다. 다시 로그인해 주세요."
        : reason === "auth-required"
          ? "로그인이 필요한 페이지입니다."
          : undefined;

  return (
    <PublicOnlyRoute>
      <main className="flex min-h-screen items-center justify-center bg-zinc-50 px-6 py-12">
        <section className="w-full max-w-md rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm">
          <Link href="/" className="text-sm text-zinc-500 hover:text-zinc-900">
            ← 홈으로
          </Link>
          <h1 className="mt-6 text-2xl font-semibold tracking-tight">로그인</h1>
          <p className="mt-2 mb-8 text-sm text-zinc-600">
            면접 연습 기록을 이어서 관리하세요.
          </p>
          <LoginForm initialMessage={initialMessage} />
        </section>
      </main>
    </PublicOnlyRoute>
  );
}
