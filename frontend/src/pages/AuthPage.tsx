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
}

const authContent = {
  login: {
    eyebrow: 'Secure sign in',
    title: '위협 인텔리전스 대시보드에 로그인',
    description:
      '운영 팀 계정으로 접속해 실시간 URL 수집 현황과 대응 지표를 확인하세요.',
    primaryLabel: '로그인',
    secondaryPrompt: '아직 계정이 없나요?',
    secondaryAction: '회원가입',
    fields: [
      { label: '업무용 이메일', type: 'email', placeholder: 'security@company.com' },
      { label: '비밀번호', type: 'password', placeholder: '비밀번호를 입력하세요' },
    ],
  },
  signup: {
    eyebrow: 'Create workspace access',
    title: '구글 OAuth로 빠르게 회원가입',
    description:
      '보안 운영 조직용 워크스페이스를 생성하고 URL 분석 리포트와 신고 흐름을 연결합니다.',
    primaryLabel: '회원가입',
    secondaryPrompt: '이미 계정이 있나요?',
    secondaryAction: '로그인',
    fields: [
      { label: '업무용 이메일', type: 'email', placeholder: 'security@company.com' },
      { label: '조직명', type: 'text', placeholder: 'Team Korea Security' },
      { label: '비밀번호', type: 'password', placeholder: '비밀번호를 설정하세요' },
    ],
  },
} as const;

const highlights = [
  { label: 'Google OAuth', value: 'SSO 준비 완료' },
  { label: '평균 초기 설정', value: '5분 이내' },
  { label: '위협 URL 탐지', value: '실시간 연동' },
];

function AuthPage({
  mode,
  theme,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
}: AuthPageProps) {
  const content = authContent[mode];

  return (
    <div className="dashboard-shell">
      <Header
        currentView={mode}
        theme={theme}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onToggleTheme={onToggleTheme}
      />

      <main className="auth-main">
        <section className="auth-layout">
          <section className="auth-showcase">
            <p className="eyebrow">{content.eyebrow}</p>
            <h1>{content.title}</h1>
            <p className="hero-text">{content.description}</p>

            <div className="auth-highlights">
              {highlights.map((item) => (
                <article key={item.label} className="auth-highlight-card">
                  <span>{item.label}</span>
                  <strong>{item.value}</strong>
                </article>
              ))}
            </div>

            <div className="auth-proof">
              <p className="auth-proof-label">보안 접근 제어</p>
              <strong>Google Workspace 계정으로 인증 흐름을 단순화합니다.</strong>
              <p>
                실제 OAuth 연동 시 백엔드 콜백 URL과 클라이언트 ID만 연결하면 바로 적용할
                수 있는 화면 구조입니다.
              </p>
            </div>
          </section>

          <section className="auth-card">
            <div className="auth-card-head">
              <p className="eyebrow">{mode === 'login' ? 'Welcome back' : 'Join URL GUARD'}</p>
              <h2>{content.primaryLabel}</h2>
              <p>{content.description}</p>
            </div>

            <button className="google-button" type="button">
              <span className="google-mark" aria-hidden="true">
                G
              </span>
              Google로 {content.primaryLabel} 계속하기
            </button>

            <div className="auth-divider">
              <span>또는 이메일로 계속</span>
            </div>

            <form className="auth-form">
              {content.fields.map((field) => (
                <label key={field.label} className="auth-field">
                  <span>{field.label}</span>
                  <input placeholder={field.placeholder} type={field.type} />
                </label>
              ))}

              {mode === 'signup' ? (
                <label className="auth-check">
                  <input type="checkbox" />
                  <span>이용약관 및 개인정보 처리방침에 동의합니다.</span>
                </label>
              ) : null}

              <button className="primary-button auth-submit" type="button">
                {content.primaryLabel}
              </button>
            </form>

            <div className="auth-footer">
              <span>{content.secondaryPrompt}</span>
              <button
                className="auth-switch"
                onClick={mode === 'login' ? onGoSignup : onGoLogin}
                type="button"
              >
                {content.secondaryAction}
              </button>
            </div>
          </section>
        </section>
      </main>
    </div>
  );
}

export default AuthPage;
