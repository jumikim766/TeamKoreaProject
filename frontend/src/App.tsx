import { useEffect, useState } from "react";
import AuthPage from "./pages/AuthPage";
import ClassificationPage from "./pages/ClassificationPage";
import Dashboard from "./pages/Dashboard";
import MailPage from "./pages/MailPage";
import MyPage from "./pages/MyPage";
import NotificationPage from "./pages/NotificationPage";
import OAuthCallbackPage from "./pages/OAuthCallbackPage";
import ReportPage from "./pages/ReportPage";
import GuidePage from "./pages/GuidePage";
import SimplePage from "./pages/SimplePage";
import TermsPage from "./pages/TermsPage";
import PrivacyPage from "./pages/PrivacyPage";
import UrlPage from "./pages/UrlPage";
import SecurityContactPage from "./pages/SecurityContactPage";
import "./styles/Dashboard.css";
import { clearAccessToken, getAccessToken } from "./utils/token";
import { logout } from "./api/authApi";
import { getUnreadCount } from "./api/notificationApi";

type ThemeMode = "light" | "dark";

export type ViewMode =
  | "dashboard"
  | "login"
  | "signup"
  | "find-username"
  | "password-reset"
  | "mypage"
  | "my-mailbox"
  | "mail-connect"
  | "url-statistics"
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
  | "security-contact"
  | "guide";

const viewToPath: Record<ViewMode, string> = {
  dashboard: "/",
  login: "/login",
  signup: "/signup",
  "find-username": "/find-username",
  "password-reset": "/password-reset",
  mypage: "/mypage",
  "my-mailbox": "/my-mailbox",
  "mail-connect": "/mail-connect",
  "my-url": "/my-url","url-statistics": "/url-statistics",
  "url-library": "/url-library",
  notifications: "/notifications",
  "notification-settings": "/notification-settings",
  "report-guide": "/report-guide",
  report: "/report",
  "classification-method": "/classification-method",
  "classification-criteria": "/classification-criteria",
  "service-info": "/service-info",
  terms: "/terms",
  privacy: "/privacy",
  "security-contact": "/security-contact",
  guide: "/guide",
};

const pathToView: Record<string, ViewMode> = Object.fromEntries(
  Object.entries(viewToPath).map(([view, path]) => [path, view]),
) as Record<string, ViewMode>;

function getViewFromPath(): ViewMode {
  return pathToView[window.location.pathname] || "dashboard";
}

function getInitialTheme(): ThemeMode {
  const savedTheme = window.localStorage.getItem("theme-mode");

  if (savedTheme === "light" || savedTheme === "dark") {
    return savedTheme;
  }

  return window.matchMedia("(prefers-color-scheme: dark)").matches
    ? "dark"
    : "light";
}

function getSavedUserName() {
  return (
    window.localStorage.getItem("userName") ||
    window.localStorage.getItem("name") ||
    window.localStorage.getItem("nickname") ||
    "사용자"
  );
}

const pageContent: Record<
  "service-info" | "terms" | "privacy" | "security-contact",
  { title: string; description: string }
> = {
  "service-info": {
    title: "서비스 소개",
    description: "URL GUARD 서비스의 목적과 주요 기능을 소개하는 화면입니다.",
  },
  terms: {
    title: "이용약관",
    description: "서비스 이용 조건과 운영 정책을 확인할 수 있는 화면입니다.",
  },
  privacy: {
    title: "개인정보 처리방침",
    description: "개인정보 수집 및 이용 방침을 확인할 수 있는 화면입니다.",
  },
  "security-contact": {
    title: "보안 문의",
    description: "보안 관련 문의 및 신고 접수 채널을 안내하는 화면입니다.",
  },
};

function App() {
  const [theme, setTheme] = useState<ThemeMode>(getInitialTheme);
  const [view, setView] = useState<ViewMode>(getViewFromPath);
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(
    Boolean(getAccessToken()),
  );
  const [userName, setUserName] = useState<string>(getSavedUserName);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem("theme-mode", theme);
  }, [theme]);
  const refreshLoginState = () => {
    const hasToken = Boolean(getAccessToken());

    setIsLoggedIn(hasToken);
    setUserName(hasToken ? getSavedUserName() : "사용자");
  };


  useEffect(() => {
    if (!isLoggedIn) {
      setUnreadCount(0);
      return;
    }

    const fetchUnreadCount = async () => {
      try {
        const count = await getUnreadCount();
        setUnreadCount(count);
      } catch (error) {
        console.error("읽지 않은 알림 개수 조회 실패:", error);
      }
    };

    fetchUnreadCount();
  }, [isLoggedIn, view]);

  useEffect(() => {
    const handlePopState = () => {
      setView(getViewFromPath());
      refreshLoginState();
    };

    window.addEventListener("popstate", handlePopState);

    return () => {
      window.removeEventListener("popstate", handlePopState);
    };
  }, []);

  
  const protectedViews: ViewMode[] = [
    "my-mailbox",
    "mail-connect",
    "url-statistics",
    "my-url",
    "url-library",
    "notifications",
    "notification-settings",
  ];

const handleNavigate = (nextView: ViewMode, replace = false) => {
  refreshLoginState();

  const hasToken = Boolean(getAccessToken());

  if (protectedViews.includes(nextView) && !hasToken) {
    alert("로그인이 필요한 메뉴입니다.");
    nextView = "login";
  }

  const nextPath = viewToPath[nextView];

  if (window.location.pathname !== nextPath) {
    if (replace) {
      window.history.replaceState(null, "", nextPath);
    } else {
      window.history.pushState(null, "", nextPath);
    }
  }

  setView(nextView);
};

  const handleToggleTheme = () => {
    setTheme((currentTheme) => (currentTheme === "light" ? "dark" : "light"));
  };

  const handleGoHome = () => {
    handleNavigate("dashboard");
  };

  const handleGoLogin = () => {
    handleNavigate("login");
  };

  const handleGoSignup = () => {
    handleNavigate("signup");
  };

  const handleGoMyPage = () => {
    handleNavigate("mypage");
  };

  const handleGoNotifications = () => {
    handleNavigate("notifications");
  };

  const handleLogout = async () => {
  try {
    await logout();
  } catch (error) {
    console.error("로그아웃 실패:", error);
  } finally {
    clearAccessToken();
    localStorage.removeItem("userName");
    setIsLoggedIn(false);
    setUserName("사용자");
    handleNavigate("login");
  }
};

  const sharedProps = {
    theme,
    onToggleTheme: handleToggleTheme,
    onGoHome: handleGoHome,
    onGoLogin: handleGoLogin,
    onGoSignup: handleGoSignup,
    onGoMyPage: handleGoMyPage,
    onGoNotifications: handleGoNotifications,
    unreadCount,
  };

  const authProps = {
    isLoggedIn,
    userName,
    onLogout: handleLogout,
    onNavigate: handleNavigate,
  };

  if (window.location.pathname === "/oauth/callback") {
    return (
      <OAuthCallbackPage
        onSuccess={() => {
          refreshLoginState();
          handleNavigate("dashboard", true);
        }}
        onFail={() => {
          clearAccessToken();
          setIsLoggedIn(false);
          setUserName("사용자");
          handleNavigate("login", true);
        }}
      />
    );
  }

  if (view === "login" || view === "signup" || view === "find-username" || view === "password-reset") {
    return (
      <AuthPage
        {...sharedProps}
        {...authProps}
        mode={view}
        onLoginSuccess={() => {
          refreshLoginState();
          handleNavigate("dashboard", true);
        }}
      />
    );
  }

  if (view === "dashboard") {
    return <Dashboard {...sharedProps} {...authProps} />;
  }

  if (view === "mypage") {
    return <MyPage {...sharedProps} {...authProps} />;
  }

  if (view === "my-mailbox" || view === "mail-connect") {
    return <MailPage {...sharedProps} {...authProps} currentView={view} />;
  }

  if (
  view === "url-statistics" ||
  view === "my-url" ||
  view === "url-library"
) {
  return <UrlPage {...sharedProps} {...authProps} currentView={view} />;
}

  if (view === "notifications" || view === "notification-settings") {
    return (
      <NotificationPage {...sharedProps} {...authProps} currentView={view} />
    );
  }

  if (view === "report-guide" || view === "report") {
    return <ReportPage {...sharedProps} {...authProps} currentView={view} />;
  }

  if (view === "security-contact") {
    return <SecurityContactPage {...sharedProps} {...authProps} />;
  }

  if (view === "classification-method" || view === "classification-criteria") {
    return (
      <ClassificationPage {...sharedProps} {...authProps} currentView={view} />
    );
  }

  if (view === "terms") {
    return <TermsPage {...sharedProps} {...authProps} />;
  }

  if (view === "privacy") {
    return <PrivacyPage {...sharedProps} {...authProps} />;
  }

  if (view === "guide") {
    return <GuidePage {...sharedProps} {...authProps} />;
  }

  const currentPage = pageContent[view];

  return (
    <SimplePage
      {...sharedProps}
      {...authProps}
      currentView={view}
      title={currentPage.title}
      description={currentPage.description}
    />
  );
}

export default App;