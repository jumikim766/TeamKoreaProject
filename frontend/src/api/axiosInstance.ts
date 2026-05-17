import axios from "axios";
import { clearAccessToken, saveAccessToken, getAccessToken } from "../utils/token";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

// 인증/공용용. /api prefix 포함 호출은 호출부에서.
export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
  withCredentials: true, // ← refreshToken 쿠키 송수신에 필수
});

// 요청 인터셉터: accessToken 자동 부착
apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();
  config.headers = config.headers ?? {};
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// 응답 인터셉터: 401 시 /api/auth/reissue 자동 호출 (쿠키 기반)
apiClient.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;

    if (!original) {
    return Promise.reject(error);
    }

    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      try {
        const res = await axios.post(
          `${BASE_URL}/api/auth/reissue`,
          {},
          { withCredentials: true } // body 없음. 쿠키만.
        );
        const newAccessToken = res.data.data.accessToken;
        saveAccessToken(newAccessToken);
        original.headers = original.headers ?? {};
        original.headers.Authorization = `Bearer ${newAccessToken}`;
        return apiClient(original);
      } catch (e) {
        clearAccessToken();
        window.location.href = "/login"; // 또는 로그인 화면
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;