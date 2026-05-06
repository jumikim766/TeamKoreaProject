import { useMemo, useState } from 'react';
import apiClient from '../api/axiosInstance';
import Header from '../components/Header';
import '../styles/Dashboard.css';
import { saveTokens } from '../utils/token';

type ThemeMode = 'light' | 'dark';
type AuthMode = 'login' | 'signup';

interface AuthPageProps {
  mode: AuthMode;
  theme: ThemeMode;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

interface LoginResponseData {
  accessToken: string;
  refreshToken: string;
  tokenType?: string;
}

function isValidEmail(email: string) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function AuthPage({
  mode,
  theme,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
}: AuthPageProps) {
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  const [signupUsername, setSignupUsername] = useState('');
  const [signupEmail, setSignupEmail] = useState('');
  const [signupPassword, setSignupPassword] = useState('');
  const [signupName, setSignupName] = useState('');

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [authMessage, setAuthMessage] = useState('');

  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

  const isLoginValid = useMemo(() => {
    return isValidEmail(loginEmail) && loginPassword.trim().length >= 1;
  }, [loginEmail, loginPassword]);

  const isSignupValid = useMemo(() => {
    return (
      signupUsername.trim().length >= 3 &&
      isValidEmail(signupEmail) &&
      signupPassword.trim().length >= 8 &&
      signupName.trim().length >= 2
    );
  }, [signupUsername, signupEmail, signupPassword, signupName]);

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  const handleNaverLogin = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/naver';
  };

  const handleLogin = async () => {
    if (!isLoginValid || isSubmitting) return;

    try {
      setIsSubmitting(true);
      setAuthMessage('');

      const response = await apiClient.post<ApiResponse<LoginResponseData>>('/api/auth/login', {
        email: loginEmail,
        password: loginPassword,
      });

      const { accessToken, refreshToken } = response.data.data;

      saveTokens(accessToken, refreshToken);

      alert(response.data.message || '로그인에 성공했습니다.');
      onGoHome();
    } catch (error) {
      console.error(error);
      setAuthMessage('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSignup = async () => {
    if (!isSignupValid || isSubmitting) return;

    try {
      setIsSubmitting(true);
      setAuthMessage('');

      await apiClient.post('/api/auth/signup', {
        username: signupUsername,
        email: signupEmail,
        password: signupPassword,
        name: signupName,
      });

      alert('회원가입이 완료되었습니다. 로그인 화면으로 이동합니다.');
      onGoLogin();
    } catch (error) {
      console.error(error);
      setAuthMessage('회원가입에 실패했습니다. 입력값 또는 중복 여부를 확인해주세요.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleGoogleSocialAuth = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
  };

  const handleNaverSocialAuth = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/naver`;
  };

  return (
    <div className={`dashboard-shell ${theme}`}>
      <Header
        currentView={mode}
        theme={theme}
        isLoggedIn={false}
        onLogout={() => {}}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onGoMyPage={onGoMyPage}
        onToggleTheme={onToggleTheme}
      />

      <main className="auth-main auth-main-centered">
        <section className="auth-layout auth-layout-centered">
          <section className="auth-card auth-card-centered">
            <div className="auth-card-head">
              <p className="eyebrow">{mode === 'login' ? 'WELCOME BACK' : 'JOIN URL GUARD'}</p>
              <h2>{mode === 'login' ? '로그인' : '회원가입'}</h2>
              <p>
                {mode === 'login'
                  ? '이메일과 비밀번호를 입력해 로그인할 수 있습니다.'
                  : '아이디, 이메일, 비밀번호, 이름을 입력해 회원가입할 수 있습니다.'}
              </p>
            </div>

            <div className="social-button-group">
              <button className="google-button" type="button" onClick={handleGoogleSocialAuth}>
                <span className="google-mark" aria-hidden="true">
                  G
                </span>
                Google로 {mode === 'login' ? '로그인' : '회원가입'} 계속하기
              </button>

              <button className="naver-button" type="button" onClick={handleNaverSocialAuth}>
                <span className="naver-mark" aria-hidden="true">
                  N
                </span>
                naver로 {mode === 'login' ? '로그인' : '회원가입'} 계속하기
              </button>
            </div>

            <div className="auth-divider">
              <span>또는 이메일로 계속</span>
            </div>

            {mode === 'login' ? (
              <form className="auth-form">
                <label className="auth-field">
                  <span>이메일</span>
                  <input
                    placeholder="minseo@example.com"
                    type="email"
                    value={loginEmail}
                    onChange={(event) => setLoginEmail(event.target.value)}
                  />
                </label>

                <label className="auth-field">
                  <span>비밀번호</span>
                  <input
                    placeholder="비밀번호를 입력하세요"
                    type="password"
                    value={loginPassword}
                    onChange={(event) => setLoginPassword(event.target.value)}
                  />
                </label>

                <div className="auth-inline-actions">
                  <button className="auth-text-button" type="button">
                    비밀번호 찾기
                  </button>
                </div>

                <button
                  className={`primary-button auth-submit ${!isLoginValid ? 'is-disabled' : ''}`}
                  disabled={!isLoginValid || isSubmitting}
                  onClick={handleLogin}
                  type="button"
                >
                  {isSubmitting ? '로그인 중...' : '로그인'}
                </button>
              </form>
            ) : (
              <form className="auth-form">
                <label className="auth-field">
                  <span>아이디</span>
                  <input
                    placeholder="minseo"
                    type="text"
                    value={signupUsername}
                    onChange={(event) => setSignupUsername(event.target.value)}
                  />
                </label>

                <label className="auth-field">
                  <span>이메일</span>
                  <input
                    placeholder="minseo@example.com"
                    type="email"
                    value={signupEmail}
                    onChange={(event) => setSignupEmail(event.target.value)}
                  />
                </label>

                <label className="auth-field">
                  <span>비밀번호</span>
                  <input
                    placeholder="1234qwer!"
                    type="password"
                    value={signupPassword}
                    onChange={(event) => setSignupPassword(event.target.value)}
                  />
                </label>

                <label className="auth-field">
                  <span>이름</span>
                  <input
                    placeholder="송민서"
                    type="text"
                    value={signupName}
                    onChange={(event) => setSignupName(event.target.value)}
                  />
                </label>

                <button
                  className={`primary-button auth-submit ${!isSignupValid ? 'is-disabled' : ''}`}
                  disabled={!isSignupValid || isSubmitting}
                  onClick={handleSignup}
                  type="button"
                >
                  {isSubmitting ? '회원가입 중...' : '회원가입'}
                </button>
              </form>
            )}

            {authMessage ? <p className="auth-error-message">{authMessage}</p> : null}

            <div className="auth-footer">
              <span>{mode === 'login' ? '아직 계정이 없나요?' : '이미 계정이 있나요?'}</span>
              <button
                className="auth-switch"
                onClick={mode === 'login' ? onGoSignup : onGoLogin}
                type="button"
              >
                {mode === 'login' ? '회원가입' : '로그인'}
              </button>
            </div>
          </section>
        </section>
      </main>
    </div>
  );
}

export default AuthPage;