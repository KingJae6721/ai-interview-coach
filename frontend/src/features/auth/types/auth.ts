export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
}

export interface SignupResponse {
  id: number;
  email: string;
  nickname: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export type UserRole = "USER" | "ADMIN";

export interface AuthUser {
  id: number;
  email: string;
  nickname: string;
  role: UserRole;
}

export interface LoginResponse extends AuthUser {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface ReissueRequest {
  refreshToken: string;
}

export interface ReissueResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface AuthSession {
  user: AuthUser;
  accessToken: string;
  refreshToken: string;
}
