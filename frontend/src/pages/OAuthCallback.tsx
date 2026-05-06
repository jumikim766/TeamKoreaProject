import { useEffect } from 'react';
import { saveTokens } from '../utils/token';
import { useNavigate } from 'react-router-dom';

function OAuthCallbackPage() {
  const navigate = useNavigate();
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const accessToken = params.get('accessToken');
    const refreshToken = params.get('refreshToken');

    if (accessToken && refreshToken) {
      saveTokens(accessToken, refreshToken);

      alert('로그인이 성공되었습니다.');

      // 토큰 저장 후 메인으로 이동
      navigate('/', { replace: true });
    } else {
      alert('로그인에 실패했습니다.');
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  return <div>로그인 처리 중...</div>;
}

export default OAuthCallbackPage;