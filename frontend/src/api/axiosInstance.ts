import axios from 'axios';
import type { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { getAccessToken, getRefreshToken, removeTokens, saveTokens } from '../utils/token';

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

interface ReissueResponseData {
  accessToken: string;
  tokenType: string;
}

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const BASE_URL = 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const accessToken = getAccessToken();

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;

    if (error.response?.status !== 401 || !originalRequest || originalRequest._retry) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    const refreshToken = getRefreshToken();

    if (!refreshToken) {
      removeTokens();
      window.location.href = '/';
      return Promise.reject(error);
    }

    try {
      const reissueResponse = await axios.post<ApiResponse<ReissueResponseData>>(
        `${BASE_URL}/api/auth/reissue`,
        { refreshToken },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        },
      );

      const newAccessToken = reissueResponse.data.data.accessToken;

      saveTokens(newAccessToken, refreshToken);
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

      return apiClient(originalRequest);
    } catch (reissueError) {
      removeTokens();
      window.location.href = '/';
      return Promise.reject(reissueError);
    }
  },
);

export default apiClient;