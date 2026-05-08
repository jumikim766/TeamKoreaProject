import type { ViewMode } from '../App';

type HeaderProps = {
  theme: 'light' | 'dark';
  currentView: ViewMode;
  isLoggedIn?: boolean;
  userName?: string;
  onLogout?: () => void;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
};

function Header({
  theme,
  currentView,
  isLoggedIn = false,
  userName = '팀코',
  onLogout,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
}: HeaderProps) {
  return (
    <header className="header">
      <button className="brand" type="button" onClick={onGoHome}>
        <span className="brand-mark">UG</span>
        <span>
          <strong>URL GUARD</strong>
          <small>피싱 URL 보안 관리</small>
        </span>
      </button>

      <div className="header-actions">
        <button type="button" className="theme-toggle" onClick={onToggleTheme}>
          {theme === 'light' ? 'DARK' : 'LIGHT'}
        </button>

        {isLoggedIn ? (
          <>
            {userName && <span className="user-name">{userName} 님</span>}

            <button
              type="button"
              className={currentView === 'mypage' ? 'active' : ''}
              onClick={onGoMyPage}
            >
              마이페이지
            </button>

            <button type="button" onClick={onLogout}>
              로그아웃
            </button>
          </>
        ) : (
          <>
            <button
              type="button"
              className={currentView === 'login' ? 'active' : ''}
              onClick={onGoLogin}
            >
              로그인
            </button>

            <button
              type="button"
              className={currentView === 'signup' ? 'active' : ''}
              onClick={onGoSignup}
            >
              회원가입
            </button>
          </>
        )}
      </div>
    </header>
  );
}

export default Header;