import type { AuthSession } from "@/features/auth/types/auth";

const AUTH_SESSION_KEY = "ai-interview-coach.auth-session";
export const AUTH_EXPIRED_EVENT = "auth:expired";
const listeners = new Set<() => void>();
let cachedSerializedSession: string | null = null;
let cachedSession: AuthSession | null = null;

function getSessionStorage(): Storage | null {
  return typeof window === "undefined" ? null : window.sessionStorage;
}

export function getAuthSession(): AuthSession | null {
  const serializedSession = getSessionStorage()?.getItem(AUTH_SESSION_KEY);

  if (!serializedSession) {
    cachedSerializedSession = null;
    cachedSession = null;
    return null;
  }

  if (serializedSession === cachedSerializedSession) {
    return cachedSession;
  }

  try {
    cachedSerializedSession = serializedSession;
    cachedSession = JSON.parse(serializedSession) as AuthSession;
    return cachedSession;
  } catch {
    clearAuthSession();
    return null;
  }
}

export function saveAuthSession(session: AuthSession): void {
  const serializedSession = JSON.stringify(session);
  cachedSerializedSession = serializedSession;
  cachedSession = session;
  getSessionStorage()?.setItem(AUTH_SESSION_KEY, serializedSession);
  listeners.forEach((listener) => listener());
}

export function clearAuthSession(): void {
  cachedSerializedSession = null;
  cachedSession = null;
  getSessionStorage()?.removeItem(AUTH_SESSION_KEY);
  listeners.forEach((listener) => listener());
}

export function subscribeAuthSession(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function getAccessToken(): string | null {
  return getAuthSession()?.accessToken ?? null;
}

export function getRefreshToken(): string | null {
  return getAuthSession()?.refreshToken ?? null;
}

export function updateAuthTokens(
  accessToken: string,
  refreshToken: string,
): void {
  const session = getAuthSession();

  if (!session) {
    throw new Error("저장된 인증 세션이 없습니다.");
  }

  saveAuthSession({ ...session, accessToken, refreshToken });
}

export function expireAuthSession(): void {
  const hadSession = getAuthSession() !== null;
  clearAuthSession();

  if (hadSession && typeof window !== "undefined") {
    window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
  }
}
