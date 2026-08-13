import Link from "next/link";

import { PublicOnlyRoute } from "@/features/auth/components/route-guards";
import { SignupForm } from "@/features/auth/components/signup-form";

export default function SignupPage() {
  return (
    <PublicOnlyRoute>
      <main className="flex min-h-screen items-center justify-center bg-zinc-50 px-6 py-12">
        <section className="w-full max-w-md rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm">
          <Link href="/" className="text-sm text-zinc-500 hover:text-zinc-900">
            ← 홈으로
          </Link>
          <h1 className="mt-6 text-2xl font-semibold tracking-tight">
            회원가입
          </h1>
          <p className="mt-2 mb-8 text-sm text-zinc-600">
            AI 면접 코칭을 위한 계정을 만드세요.
          </p>
          <SignupForm />
        </section>
      </main>
    </PublicOnlyRoute>
  );
}
