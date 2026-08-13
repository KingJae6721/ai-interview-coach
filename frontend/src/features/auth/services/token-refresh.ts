import { getApiBaseUrl } from "@/lib/env";
import {
  expireAuthSession,
  getRefreshToken,
  updateAuthTokens,
} from "@/features/auth/lib/auth-storage";
import type { ReissueResponse } from "@/features/auth/types/auth";
import type { ApiResponse } from "@/types/api";

let refreshPromise: Promise<string> | null = null;

async function requestNewAccessToken(): Promise<string> {
  const refreshToken = getRefreshToken();

  if (!refreshToken) {
    throw new Error("Refresh Token이 없습니다.");
  }

  const response = await fetch(`${getApiBaseUrl()}/api/v1/auth/reissue`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
    cache: "no-store",
  });
  const body = (await response
    .json()
    .catch(() => null)) as ApiResponse<ReissueResponse> | null;

  if (!response.ok || !body?.success || !body.data) {
    throw new Error(body?.message ?? "Access Token 재발급에 실패했습니다.");
  }

  updateAuthTokens(body.data.accessToken, body.data.refreshToken);
  return body.data.accessToken;
}

export function refreshAccessToken(): Promise<string> {
  if (!refreshPromise) {
    refreshPromise = requestNewAccessToken()
      .catch((error: unknown) => {
        expireAuthSession();
        throw error;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}
