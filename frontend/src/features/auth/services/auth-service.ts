import { apiFetch } from "@/services/api-client";
import type {
  LoginRequest,
  LoginResponse,
  ReissueResponse,
  SignupRequest,
  SignupResponse,
} from "@/features/auth/types/auth";

export function signup(request: SignupRequest): Promise<SignupResponse> {
  return apiFetch<SignupResponse>("/api/v1/auth/signup", {
    method: "POST",
    body: JSON.stringify(request),
    authenticated: false,
  });
}

export function login(request: LoginRequest): Promise<LoginResponse> {
  return apiFetch<LoginResponse>("/api/v1/auth/login", {
    method: "POST",
    body: JSON.stringify(request),
    authenticated: false,
  });
}

export function reissue(refreshToken: string): Promise<ReissueResponse> {
  return apiFetch<ReissueResponse>("/api/v1/auth/reissue", {
    method: "POST",
    body: JSON.stringify({ refreshToken }),
    authenticated: false,
  });
}

export function logout(): Promise<void> {
  return apiFetch<void>("/api/v1/auth/logout", { method: "POST" });
}
