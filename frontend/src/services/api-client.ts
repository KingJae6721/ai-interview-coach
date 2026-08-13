import { getApiBaseUrl } from "@/lib/env";
import type { ApiResponse } from "@/types/api";

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export async function apiFetch<T>(
  path: string,
  init: RequestInit = {},
): Promise<ApiResponse<T>> {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const headers = new Headers(init.headers);

  if (
    init.body &&
    !(init.body instanceof FormData) &&
    !headers.has("Content-Type")
  ) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${getApiBaseUrl()}${normalizedPath}`, {
    ...init,
    headers,
    cache: init.cache ?? "no-store",
  });
  const body = (await response.json()) as ApiResponse<T>;

  if (!response.ok || !body.success) {
    throw new ApiError(body.message, response.status, body.code);
  }

  return body;
}
