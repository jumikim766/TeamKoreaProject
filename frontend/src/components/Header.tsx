type ViewMode =
  | "dashboard"
  | "login"
  | "signup"
  | "mypage"
  | "service-info"
  | "terms"
  | "privacy"
  | "security-contact";

type HeaderProps = {
  theme: "light" | "dark";
  currentView: ViewMode;
  isLoggedIn: boolean;
  onLogout: () => void;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
};

function Header({
  theme,
  currentView,
  isLoggedIn,
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
          <small>Enterprise phishing intelligence</small>
        </span>
      </button>

      <div className="header-actions">
        <button type="button" className="theme-toggle" onClick={onToggleTheme}>
          {theme === "light" ? "Dark" : "Light"}
        </button>

        {isLoggedIn ? (
          <>
            <button
              type="button"
              className={currentView === "mypage" ? "active" : ""}
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
              className={currentView === "login" ? "active" : ""}
              onClick={onGoLogin}
            >
              로그인
            </button>

            <button
              type="button"
              className={currentView === "signup" ? "active" : ""}
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