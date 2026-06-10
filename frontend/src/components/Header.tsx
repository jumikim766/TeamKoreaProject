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
  onGoNotifications?: () => void;
  unreadCount?: number;
}

function BellIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M10.268 21a2 2 0 0 0 3.464 0" />
      <path d="M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326" />
    </svg>
  );
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
  onGoNotifications,
  unreadCount = 0,
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
            <button
              className="header-bell-button"
              style={{
                width: 'auto',
                height: 'auto',
                minWidth: 0,
                border: 'none',
                background: 'transparent',
                boxShadow: 'none',
                borderRadius: 0,
                padding: 0,
                margin: '0 10px 0 0',
              }}
              onClick={onGoNotifications}
              type="button"
              aria-label={`알림함으로 이동${unreadCount > 0 ? `, 읽지 않은 알림 ${unreadCount}개` : ''}`}
            >
              <BellIcon />
              {unreadCount > 0 && (
                <span className="header-bell-badge">
                  {unreadCount > 99 ? '99+' : unreadCount}
                </span>
              )}
            </button>

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