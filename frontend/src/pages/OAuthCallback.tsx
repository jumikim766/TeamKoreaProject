import { useEffect } from 'react';
import { saveTokens } from '../utils/token';

function OAuthCallbackPage() {
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);

    const accessToken = params.get('accessToken');
    const refreshToken = params.get('refreshToken');

    if (accessToken && refreshToken) {
      saveTokens(accessToken, refreshToken);
      alert('소셜 로그인 성공');
      window.location.href = '/';
    } else {
      alert('로그인 실패');
      window.location.href = '/login';
    }
  }, []);

  return <div>로그인 처리 중...</div>;
}

export default OAuthCallbackPage;