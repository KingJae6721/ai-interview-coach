import { ApiError } from "@/services/api-client";
import type { ValidationError } from "@/types/api";

export function getErrorMessage(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return "요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
  }

  if (Array.isArray(error.data)) {
    const validationErrors = error.data as ValidationError[];
    return validationErrors.map(({ reason }) => reason).join(" ");
  }

  return error.message;
}
