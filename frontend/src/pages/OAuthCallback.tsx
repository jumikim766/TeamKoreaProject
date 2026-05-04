import { useEffect } from 'react';
import { saveTokens } from '../utils/token';
import '../styles/Dashboard.css';

interface OAuthCallbackProps {
  onGoHome: () => void;
}

function OAuthCallback({ onGoHome }: OAuthCallbackProps) {
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const accessToken = params.get('accessToken');
    const refreshToken = params.get('refreshToken');

    if (accessToken && refreshToken) {
      saveTokens(accessToken, refreshToken);
      onGoHome();
      return;
    }

    alert('소셜 로그인 토큰을 확인할 수 없습니다.');
    onGoHome();
  }, [onGoHome]);

  return (
    <div className="dashboard-shell">
      <main className="auth-main auth-main-centered">
        <section className="auth-layout auth-layout-centered">
          <section className="auth-card auth-card-centered">
            <div className="auth-card-head">
              <p className="eyebrow">OAuth callback</p>
              <h2>로그인 처리 중</h2>
              <p>소셜 로그인 정보를 확인하고 있습니다.</p>
            </div>
          </section>
        </section>
      </main>
    </div>
  );
}

export default OAuthCallback;