const KEY = "accessToken";

export const saveAccessToken = (token: string) => localStorage.setItem(KEY, token);
export const getAccessToken = () => localStorage.getItem(KEY);
export const clearAccessToken = () => localStorage.removeItem(KEY);

// 호환용 (App.tsx에서 사용 중) — 점진적 마이그레이션
export const clearTokens = clearAccessToken;
// saveTokens / getRefreshToken은 export하지 않음 (호출부 제거 대상)