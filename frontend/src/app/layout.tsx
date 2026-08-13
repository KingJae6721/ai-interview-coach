import type { Metadata } from "next";

import { AuthProvider } from "@/features/auth/context/auth-context";

import "./globals.css";

export const metadata: Metadata = {
  title: "AI Interview Coach",
  description: "AI 기반 맞춤형 면접 코칭 서비스",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="ko">
      <body>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
