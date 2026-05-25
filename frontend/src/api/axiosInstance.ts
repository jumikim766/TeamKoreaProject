import axios from "axios";
import { clearAccessToken, saveAccessToken, getAccessToken } from "../utils/token";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

const publicAuthPaths = [
  "/api/auth/login",
  "/api/auth/signup",
  "/api/auth/signup/send-code",
  "/api/auth/signup/verify-code",
  "/api/auth/find-username/send-code",
  "/api/auth/find-username/verify-code",
  "/api/auth/password-reset/send-code",
  "/api/auth/password-reset",
  "/api/auth/reissue",
];

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
  withCredentials: true,
});

apiClient.interceptors.request.use((config) => {
  const requestUrl = config.url ?? "";

  const isPublicAuthRequest = publicAuthPaths.some((path) =>
    requestUrl.includes(path)
  );

  config.headers = config.headers ?? {};

  if (!isPublicAuthRequest) {
    const token = getAccessToken();

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  } else {
    delete config.headers.Authorization;
  }

  return config;
});

apiClient.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;

    const requestUrl = original?.url ?? "";
    const isPublicAuthRequest = publicAuthPaths.some((path) =>
      requestUrl.includes(path)
    );

    if (isPublicAuthRequest) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && original && !original._retry) {
      original._retry = true;

      try {
        const res = await axios.post(
          `${BASE_URL}/api/auth/reissue`,
          {},
          { withCredentials: true }
        );

        const newAccessToken = res.data.data.accessToken;
        saveAccessToken(newAccessToken);

        original.headers = original.headers ?? {};
        original.headers.Authorization = `Bearer ${newAccessToken}`;

        return apiClient(original);
      } catch (e) {
        clearAccessToken();
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;