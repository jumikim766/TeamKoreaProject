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
  name?: string;
  username?: string;
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