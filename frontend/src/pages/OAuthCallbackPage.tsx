import { useEffect, useRef } from 'react';
import { saveAccessToken } from '../utils/token';

type OAuthCallbackPageProps = {
  onSuccess: () => void;
  onFail: () => void;
};

function OAuthCallbackPage({
  onSuccess,
  onFail,
}: OAuthCallbackPageProps) {
  const handledRef = useRef(false);

  useEffect(() => {
    if (handledRef.current) return;
    handledRef.current = true;

    const params = new URLSearchParams(window.location.search);

    const accessToken = params.get('accessToken');

    if (accessToken) {
      saveAccessToken(accessToken);

      onSuccess();

      alert('로그인이 성공되었습니다.');
      window.location.replace('/');
    } else {
      onFail();

      alert('로그인에 실패했습니다.');
      window.location.replace('/');
    }
  }, [onSuccess, onFail]);

  return <div>로그인 처리 중...</div>;
}

export default OAuthCallbackPage;