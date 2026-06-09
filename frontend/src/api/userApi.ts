import apiClient from "./axiosInstance";
import type { BaseResponse } from "./authApi";

export interface UserMe {
  userId: number;
  email: string;
  username: string;
  name: string;
  role: string;
  provider: "LOCAL" | "GOOGLE" | "NAVER";
  gender: string | null;
  age: number | null;
  status: string;
  lastLoginAt: string | null;
  createdAt: string;
  phoneMasked: string | null; // "010****5678"
}

export const getMe = () =>
  apiClient.get<BaseResponse<UserMe>>("/api/users/me");

export interface UserUpdatePayload {
  phone?: string;   // 01012345678 (하이픈 제거 후)
  email?: string;
  gender?: "MALE" | "FEMALE" | "OTHER";
  age?: number;
}
export const updateMe = (payload: UserUpdatePayload) =>
  apiClient.patch<BaseResponse<UserMe>>("/api/users/me", payload);

export const changePassword = (currentPassword: string, newPassword: string) =>
  apiClient.patch<BaseResponse<null>>("/api/users/me/password", {
    currentPassword,
    newPassword,
  });

// LOCAL 계정만 password 필요. 소셜은 body 비우거나 password: null
export const deleteMe = (password?: string | null) =>
  apiClient.delete<BaseResponse<null>>("/api/users/me", {
    data: password ? { password } : {},
  });

// ===== 이메일 변경 인증 =====

export interface EmailChangeSendCodeRequest {
  email: string;
}

export interface EmailChangeVerifyCodeRequest {
  email: string;
  code: string;
}

// 이메일 변경 인증번호 발송
export const sendEmailChangeCode = (data: EmailChangeSendCodeRequest) =>
  apiClient.post<BaseResponse<null>>(
    "/api/users/me/email/send-code",
    data
  );

// 이메일 변경 인증번호 검증
export const verifyEmailChangeCode = (data: EmailChangeVerifyCodeRequest) =>
  apiClient.post<BaseResponse<null>>(
    "/api/users/me/email/verify-code",
    data
  );