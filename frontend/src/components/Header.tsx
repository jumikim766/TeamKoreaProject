import type { ViewMode } from '../App';

type ThemeMode = 'light' | 'dark';

interface HeaderProps {
  theme: ThemeMode;
  currentView: ViewMode;
  isLoggedIn?: boolean;
  userName?: string;
  onLogout?: () => void;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage?: () => void;
}

function Header({
  theme,
  currentView,
  isLoggedIn = false,
  userName = '사용자',
  onLogout,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
}: HeaderProps) {
  const themeLabel = theme === 'light' ? 'DARK' : 'LIGHT';

  return (
    <header className="site-header">
      <button className="brand-button" onClick={onGoHome} type="button">
        <span className="brand-mark">UG</span>
        <span className="brand-text">
          <strong>URL GUARD</strong>
          <small>Enterprise phishing intelligence</small>
        </span>
      </button>

      <div className="header-actions header-link-actions">
        {isLoggedIn ? (
          <>
            <span className="header-user-name">{userName} 님</span>
            <span className="header-divider" />

            <button
              className={currentView === 'mypage' ? 'header-text-button is-active' : 'header-text-button'}
              onClick={onGoMyPage}
              type="button"
            >
              마이페이지
            </button>

            <span className="header-divider" />

            <button className="header-text-button" onClick={onLogout} type="button">
              로그아웃
            </button>

            <span className="header-divider" />

            <button className="theme-toggle header-theme-button" onClick={onToggleTheme} type="button">
              {themeLabel}
            </button>
          </>
        ) : (
          <>
            <button
              className={currentView === 'signup' ? 'header-text-button is-active' : 'header-text-button'}
              onClick={onGoSignup}
              type="button"
            >
              회원가입
            </button>

            <span className="header-divider" />

            <button
              className={currentView === 'login' ? 'header-text-button is-active' : 'header-text-button'}
              onClick={onGoLogin}
              type="button"
            >
              로그인
            </button>

            <span className="header-divider" />

            <button className="theme-toggle header-theme-button" onClick={onToggleTheme} type="button">
              {themeLabel}
            </button>
          </>
        )}
      </div>
    </header>
  );
}

export default Header;