import { getApiBaseUrl } from "@/lib/env";
import type { ApiResponse } from "@/types/api";
import {
  expireAuthSession,
  getAccessToken,
} from "@/features/auth/lib/auth-storage";
import { refreshAccessToken } from "@/features/auth/services/token-refresh";

interface ApiRequestInit extends RequestInit {
  authenticated?: boolean;
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
    readonly data?: unknown,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function sendRequest(
  path: string,
  init: RequestInit,
  accessToken: string | null,
): Promise<Response> {
  const headers = new Headers(init.headers);

  if (
    init.body &&
    !(init.body instanceof FormData) &&
    !headers.has("Content-Type")
  ) {
    headers.set("Content-Type", "application/json");
  }

  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  return fetch(`${getApiBaseUrl()}${path}`, {
    ...init,
    headers,
    cache: init.cache ?? "no-store",
  });
}

async function parseResponse<T>(response: Response): Promise<T> {
  const body = (await response
    .json()
    .catch(() => null)) as ApiResponse<T> | null;

  if (!response.ok || !body?.success) {
    throw new ApiError(
      body?.message ?? "API 요청을 처리하지 못했습니다.",
      response.status,
      body?.code,
      body?.data,
    );
  }

  return body.data as T;
}

export async function apiFetch<T>(
  path: string,
  init: ApiRequestInit = {},
): Promise<T> {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const { authenticated, ...requestInit } = init;
  const shouldAuthenticate = authenticated !== false;

  try {
    let response = await sendRequest(
      normalizedPath,
      requestInit,
      shouldAuthenticate ? getAccessToken() : null,
    );

    if (response.status === 401 && shouldAuthenticate) {
      const newAccessToken = await refreshAccessToken();
      response = await sendRequest(normalizedPath, requestInit, newAccessToken);

      if (response.status === 401) {
        expireAuthSession();
      }
    }

    return await parseResponse<T>(response);
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }

    if (shouldAuthenticate && !getAccessToken()) {
      throw new ApiError(
        "인증이 만료되었습니다. 다시 로그인해 주세요.",
        401,
        "AUTH_SESSION_EXPIRED",
      );
    }

    throw new ApiError(
      "서버에 연결할 수 없습니다. 백엔드 실행 상태와 CORS 설정을 확인해 주세요.",
      0,
      "NETWORK_ERROR",
    );
  }
}
