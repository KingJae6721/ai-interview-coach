"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { useAuth } from "@/features/auth/context/auth-context";
import { getErrorMessage } from "@/features/auth/lib/get-error-message";

export function LoginForm({ initialMessage }: { initialMessage?: string }) {
  const router = useRouter();
  const { login } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsLoading(true);
    setErrorMessage("");

    const formData = new FormData(event.currentTarget);

    try {
      await login({
        email: String(formData.get("email")).trim(),
        password: String(formData.get("password")),
      });
      router.replace("/dashboard");
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {initialMessage && (
        <p
          role="status"
          className="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-700"
        >
          {initialMessage}
        </p>
      )}
      <div>
        <label
          htmlFor="email"
          className="block text-sm font-medium text-zinc-700"
        >
          이메일
        </label>
        <input
          id="email"
          name="email"
          type="email"
          autoComplete="email"
          required
          maxLength={255}
          disabled={isLoading}
          className="mt-2 w-full rounded-lg border border-zinc-300 px-3 py-2.5 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-200 disabled:bg-zinc-100"
        />
      </div>
      <div>
        <label
          htmlFor="password"
          className="block text-sm font-medium text-zinc-700"
        >
          비밀번호
        </label>
        <input
          id="password"
          name="password"
          type="password"
          autoComplete="current-password"
          required
          disabled={isLoading}
          className="mt-2 w-full rounded-lg border border-zinc-300 px-3 py-2.5 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-200 disabled:bg-zinc-100"
        />
      </div>
      {errorMessage && (
        <p role="alert" aria-live="polite" className="text-sm text-red-600">
          {errorMessage}
        </p>
      )}
      <button
        type="submit"
        disabled={isLoading}
        className="w-full rounded-lg bg-zinc-900 px-4 py-3 font-medium text-white hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isLoading ? "로그인 중..." : "로그인"}
      </button>
      <p className="text-center text-sm text-zinc-600">
        계정이 없으신가요?{" "}
        <Link href="/signup" className="font-medium text-zinc-900 underline">
          회원가입
        </Link>
      </p>
    </form>
  );
}
