import axios from "axios";
import type { BaseResponse } from "./authApi";

export const getErrorMessage = (error: unknown, fallback = "오류가 발생했습니다.") => {
  if (axios.isAxiosError<BaseResponse<unknown>>(error)) {
    return error.response?.data?.message ?? fallback;
  }
  return fallback;
};