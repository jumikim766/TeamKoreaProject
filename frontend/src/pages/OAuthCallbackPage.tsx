import { useEffect } from 'react';
import { saveTokens } from '../utils/token';

type OAuthCallbackPageProps = {
  onSuccess: () => void;
  onFail: () => void;
};

function OAuthCallbackPage({ onSuccess, onFail }: OAuthCallbackPageProps) {
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);

    const accessToken = params.get('accessToken');
    const refreshToken = params.get('refreshToken');

    if (accessToken && refreshToken) {
      saveTokens(accessToken, refreshToken);
      onSuccess();
      return;
    }

    onFail();
  }, [onSuccess, onFail]);

  return (
    <div className="dashboard-shell">
      <main className="auth-main auth-main-centered">
        <section className="auth-card auth-card-centered">
          <p className="eyebrow">OAUTH CALLBACK</p>
          <h2>로그인 처리 중...</h2>
          <p>소셜 로그인 정보를 확인하고 있습니다.</p>
        </section>
      </main>
    </div>
  );
}

export default OAuthCallbackPage;