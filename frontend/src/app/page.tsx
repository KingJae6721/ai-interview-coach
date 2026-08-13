import { AuthStatus } from "@/features/auth/components/auth-status";

export default function Home() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-zinc-50 px-6">
      <div className="w-full max-w-xl rounded-2xl border border-zinc-200 bg-white p-8 text-center shadow-sm">
        <h1 className="text-3xl font-semibold tracking-tight">
          AI Interview Coach
        </h1>
        <p className="mt-3 text-zinc-600">맞춤형 면접 연습을 시작해 보세요.</p>
        <AuthStatus />
      </div>
    </main>
  );
}
