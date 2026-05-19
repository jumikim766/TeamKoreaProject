const ACCESS_TOKEN_KEY = "accessToken";

export const saveAccessToken = (token: string) => {
  localStorage.setItem(ACCESS_TOKEN_KEY, token);
};

export const getAccessToken = () => {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
};

export const clearAccessToken = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
};

// 백엔드는 refreshToken을 HttpOnly 쿠키로 관리하므로 localStorage에는 저장하지 않음
// 기존 App.tsx 등에서 clearTokens()를 사용 중이라 오류 방지용으로 함수명은 유지
export const clearTokens = () => {
  clearAccessToken();
};