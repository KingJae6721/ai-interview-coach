const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;

export function getApiBaseUrl(): string {
  if (!apiBaseUrl) {
    throw new Error("NEXT_PUBLIC_API_BASE_URL 환경변수가 설정되지 않았습니다.");
  }

  return apiBaseUrl.replace(/\/$/, "");
}
