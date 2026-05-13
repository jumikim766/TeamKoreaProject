import Header from "../components/Header";
import Navbar from "../components/Navbar";

type ThemeMode = "light" | "dark";

type ViewMode =
  | "dashboard"
  | "login"
  | "signup"
  | "mypage"
  | "my-mailbox"
  | "mail-connect"
  | "my-url"
  | "url-library"
  | "notifications"
  | "notification-settings"
  | "report-guide"
  | "report"
  | "classification-method"
  | "classification-criteria"
  | "service-info"
  | "terms"
  | "privacy"
  | "security-contact";

type SimplePageView = "service-info" | "terms" | "privacy" | "security-contact";

type SimplePageProps = {
  theme: ThemeMode;
  currentView: SimplePageView;
  title: string;
  description: string;
  isLoggedIn: boolean;
  onLogout: () => void;
  onNavigate: (view: ViewMode) => void;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
};

function SimplePage({
  theme,
  currentView,
  title,
  description,
  isLoggedIn,
  onLogout,
  onNavigate,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
}: SimplePageProps) {
  return (
    <div className={`dashboard-shell ${theme}`}>
      <Header
        theme={theme}
        currentView={currentView}
        isLoggedIn={isLoggedIn}
        onLogout={onLogout}
        onToggleTheme={onToggleTheme}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onGoMyPage={onGoMyPage}
      />

      <Navbar onNavigate={onNavigate} />

      <main className="simple-page">
        <section className="simple-page-card">
          <p className="simple-page-eyebrow">URL GUARD</p>
          <h1>{title}</h1>
          <p>{description}</p>
        </section>
      </main>
    </div>
  );
}

export default SimplePage;
