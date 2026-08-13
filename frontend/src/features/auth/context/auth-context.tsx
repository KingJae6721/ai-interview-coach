"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useSyncExternalStore,
  type ReactNode,
} from "react";
import { useRouter } from "next/navigation";

import {
  AUTH_EXPIRED_EVENT,
  clearAuthSession,
  getAuthSession,
  saveAuthSession,
  subscribeAuthSession,
} from "@/features/auth/lib/auth-storage";
import * as authService from "@/features/auth/services/auth-service";
import type { AuthUser, LoginRequest } from "@/features/auth/types/auth";

interface AuthContextValue {
  user: AuthUser | null;
  isInitialized: boolean;
  login: (request: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const router = useRouter();
  const session = useSyncExternalStore(
    subscribeAuthSession,
    getAuthSession,
    () => null,
  );
  const isInitialized = useSyncExternalStore(
    () => () => undefined,
    () => true,
    () => false,
  );
  const user = session?.user ?? null;

  useEffect(() => {
    function handleAuthExpired() {
      router.replace("/login?reason=session-expired");
    }

    window.addEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired);
    return () =>
      window.removeEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired);
  }, [router]);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await authService.login(request);
    const { id, email, nickname, role, accessToken, refreshToken } = response;
    const authUser = { id, email, nickname, role };

    saveAuthSession({ user: authUser, accessToken, refreshToken });
  }, []);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      clearAuthSession();
    }
  }, []);

  const value = useMemo(
    () => ({ user, isInitialized, login, logout }),
    [user, isInitialized, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth는 AuthProvider 내부에서 사용해야 합니다.");
  }

  return context;
}
