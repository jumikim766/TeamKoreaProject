import apiClient from "./axiosInstance";

// ===== 공통 응답 =====
export interface BaseResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
  errorCode?: string;
}

// ===== 인증 =====
export interface LoginUser {
  userId: number;
  email: string;
  name: string;
  role: string;
}
export interface LoginData {
  accessToken: string;
  refreshToken: null; // 백엔드 응답상 항상 null (쿠키로 전달)
  tokenType: "Bearer";
  user: LoginUser;
}

export const login = (email: string, password: string) =>
  apiClient.post<BaseResponse<LoginData>>("/api/auth/login", { email, password });

// 회원가입: name 공백 불가, phone은 하이픈 제거 후 전송, 빈값이면 필드 자체를 안 보내는 게 안전
export interface SignupPayload {
  username: string;
  email: string;
  password: string;
  name: string;
  phone?: string;          // 010XXXXXXXX
  gender?: "MALE" | "FEMALE" | "OTHER";
  age?: number;            // 1~120
}
export const signup = (payload: SignupPayload) =>
  apiClient.post<BaseResponse<unknown>>("/api/auth/signup", payload);

export const logout = () =>
  apiClient.post<BaseResponse<null>>("/api/auth/logout"); // body 없음

export const reissue = () =>
  apiClient.post<BaseResponse<{ accessToken: string; tokenType: "Bearer" }>>(
    "/api/auth/reissue"
  ); // 보통 인터셉터가 처리하므로 수동 호출은 OAuth 콜백에서만

export const checkUsername = (username: string) =>
  apiClient.get<BaseResponse<{ available: boolean }>>(
    "/api/users/check-username",
    { params: { username } }
  );

export const checkEmail = (email: string) =>
  apiClient.get<BaseResponse<{ available: boolean }>>(
    "/api/users/check-email",
    { params: { email } }
  );

// 소셜은 별도 API 없음 — 그냥 redirect
export const goSocialLogin = (provider: "google" | "naver") => {
  const BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
  window.location.href = `${BASE}/oauth2/authorization/${provider}`;
};