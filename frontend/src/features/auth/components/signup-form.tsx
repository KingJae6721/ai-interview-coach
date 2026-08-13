"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";

import { getErrorMessage } from "@/features/auth/lib/get-error-message";
import { signup } from "@/features/auth/services/auth-service";

const PASSWORD_PATTERN = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*\W).{8,20}$/;

export function SignupForm() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");

    const formData = new FormData(event.currentTarget);
    const password = String(formData.get("password"));
    const passwordConfirm = String(formData.get("passwordConfirm"));

    if (!PASSWORD_PATTERN.test(password)) {
      setErrorMessage(
        "비밀번호는 8~20자의 영문, 숫자, 특수문자를 포함해야 합니다.",
      );
      return;
    }

    if (password !== passwordConfirm) {
      setErrorMessage("비밀번호 확인이 일치하지 않습니다.");
      return;
    }

    setIsLoading(true);

    try {
      await signup({
        email: String(formData.get("email")).trim(),
        password,
        nickname: String(formData.get("nickname")).trim(),
      });
      router.replace("/login?signup=success");
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }

  const inputClassName =
    "mt-2 w-full rounded-lg border border-zinc-300 px-3 py-2.5 outline-none focus:border-zinc-900 focus:ring-2 focus:ring-zinc-200 disabled:bg-zinc-100";

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
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
          className={inputClassName}
        />
      </div>
      <div>
        <label
          htmlFor="nickname"
          className="block text-sm font-medium text-zinc-700"
        >
          닉네임
        </label>
        <input
          id="nickname"
          name="nickname"
          type="text"
          autoComplete="nickname"
          required
          minLength={2}
          maxLength={50}
          disabled={isLoading}
          className={inputClassName}
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
          autoComplete="new-password"
          required
          minLength={8}
          maxLength={20}
          disabled={isLoading}
          className={inputClassName}
        />
        <p className="mt-1.5 text-xs text-zinc-500">
          8~20자, 영문·숫자·특수문자 포함
        </p>
      </div>
      <div>
        <label
          htmlFor="passwordConfirm"
          className="block text-sm font-medium text-zinc-700"
        >
          비밀번호 확인
        </label>
        <input
          id="passwordConfirm"
          name="passwordConfirm"
          type="password"
          autoComplete="new-password"
          required
          minLength={8}
          maxLength={20}
          disabled={isLoading}
          className={inputClassName}
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
        {isLoading ? "가입 중..." : "회원가입"}
      </button>
      <p className="text-center text-sm text-zinc-600">
        이미 계정이 있으신가요?{" "}
        <Link href="/login" className="font-medium text-zinc-900 underline">
          로그인
        </Link>
      </p>
    </form>
  );
}
