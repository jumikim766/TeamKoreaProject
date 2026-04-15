interface HeaderProps {
  theme: 'light' | 'dark';
  currentView: string;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
}

function Header({
  theme,
  currentView,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
}: HeaderProps) {
  return (
    <header className="header">
      <button className="brand-block brand-button" onClick={onGoHome} type="button">
        <div className="logo-wrap">
          <div className="logo-mark">UG</div>
          <div>
            <div className="logo">URL GUARD</div>
            <p className="brand-copy">
              메일 기반 위협 URL을 수집하고 위험도를 한 화면에서 모니터링합니다.
            </p>
          </div>
        </div>
      </button>

      <div className="header-actions">
        <button
          aria-label={theme === 'light' ? '다크모드로 전환' : '라이트모드로 전환'}
          className="theme-toggle"
          onClick={onToggleTheme}
          type="button"
        >
          <span>{theme === 'light' ? 'Dark' : 'Light'}</span>
          <strong>{theme === 'light' ? 'ON' : 'OFF'}</strong>
        </button>

        <button
          className={currentView === 'login' ? 'ghost-button is-active' : 'ghost-button'}
          onClick={onGoLogin}
          type="button"
        >
          로그인
        </button>

        <button
          className={currentView === 'signup' ? 'ghost-button is-active' : 'ghost-button'}
          onClick={onGoSignup}
          type="button"
        >
          회원가입
        </button>

        <button
          className={currentView === 'mypage' ? 'primary-button is-active' : 'primary-button'}
          onClick={onGoMyPage}
          type="button"
        >
          마이페이지
        </button>
      </div>
    </header>
  );
}

export default Header;