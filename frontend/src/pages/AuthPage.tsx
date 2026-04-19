import { useMemo, useState } from 'react';
import Header from '../components/Header';
import '../styles/Dashboard.css';

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

  const [signupName, setSignupName] = useState('');
  const [signupEmail, setSignupEmail] = useState('');
  const [emailVerified, setEmailVerified] = useState(false);
  const [agreedPrivacy, setAgreedPrivacy] = useState(false);

  const isLoginValid = useMemo(() => {
    return isValidEmail(loginEmail) && loginPassword.trim().length >= 1;
  }, [loginEmail, loginPassword]);

  const isSignupValid = useMemo(() => {
    return (
      signupName.trim().length >= 2 &&
      isValidEmail(signupEmail) &&
      emailVerified &&
      agreedPrivacy
    );
  }, [signupName, signupEmail, emailVerified, agreedPrivacy]);

  const handleSignupEmailChange = (value: string) => {
    setSignupEmail(value);
    setEmailVerified(false);
  };

  const handleVerifyEmail = () => {
    if (!isValidEmail(signupEmail)) {
      alert('올바른 이메일 형식으로 입력해주세요.');
      return;
    }

    setEmailVerified(true);
    alert('이메일 인증이 완료되었습니다.');
  };

  return (
    <div className="dashboard-shell">
      <Header
        currentView={mode}
        theme={theme}
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
              <p className="eyebrow">{mode === 'login' ? 'Welcome back' : 'Join URL GUARD'}</p>
              <h2>{mode === 'login' ? '로그인' : '회원가입'}</h2>
              <p>
                {mode === 'login'
                  ? '이메일과 비밀번호로 로그인하거나 소셜 계정으로 계속할 수 있습니다.'
                  : '간단한 정보 입력 후 이메일 인증을 완료하면 회원가입할 수 있습니다.'}
              </p>
            </div>

            <div className="social-button-group">
              <button className="google-button" type="button">
                <span className="google-mark" aria-hidden="true">
                  G
                </span>
                Google로 {mode === 'login' ? '로그인' : '회원가입'} 계속하기
              </button>

              <button className="naver-button" type="button">
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
                    placeholder="example@email.com"
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
                  type="button"
                  disabled={!isLoginValid}
                >
                  로그인
                </button>
              </form>
            ) : (
              <form className="auth-form">
                <label className="auth-field">
                  <span>사용자 이름</span>
                  <input
                    placeholder="이름을 입력하세요"
                    type="text"
                    value={signupName}
                    onChange={(event) => setSignupName(event.target.value)}
                  />
                </label>

                <label className="auth-field">
                  <span>이메일</span>
                  <input
                    placeholder="example@email.com"
                    type="email"
                    value={signupEmail}
                    onChange={(event) => handleSignupEmailChange(event.target.value)}
                  />
                </label>

                <div className="auth-verify-row">
                  <div className="auth-verify-status">
                    <span>{emailVerified ? '이메일 인증 완료' : '이메일 인증이 필요합니다.'}</span>
                  </div>
                  <button className="secondary-button auth-verify-button" type="button" onClick={handleVerifyEmail}>
                    이메일 인증
                  </button>
                </div>

                <label className="auth-check">
                  <input
                    type="checkbox"
                    checked={agreedPrivacy}
                    onChange={(event) => setAgreedPrivacy(event.target.checked)}
                  />
                  <span>개인정보 수집 및 이용에 동의합니다.</span>
                </label>

                <button
                  className={`primary-button auth-submit ${!isSignupValid ? 'is-disabled' : ''}`}
                  type="button"
                  disabled={!isSignupValid}
                >
                  회원가입
                </button>
              </form>
            )}

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