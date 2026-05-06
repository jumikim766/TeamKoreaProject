import { useEffect, useState } from 'react';
import AuthPage from './pages/AuthPage';
import ClassificationPage from './pages/ClassificationPage';
import Dashboard from './pages/Dashboard';
import MailPage from './pages/MailPage';
import MyPage from './pages/MyPage';
import NotificationPage from './pages/NotificationPage';
import OAuthCallbackPage from './pages/OAuthCallbackPage';
import ReportPage from './pages/ReportPage';
import SimplePage from './pages/SimplePage';
import UrlPage from './pages/UrlPage';
import './styles/Dashboard.css';
import { clearTokens, getAccessToken } from './utils/token';

type ThemeMode = 'light' | 'dark';

type ViewMode =
  | 'dashboard'
  | 'login'
  | 'signup'
  | 'mypage'
  | 'my-mailbox'
  | 'mail-connect'
  | 'my-url'
  | 'url-library'
  | 'notifications'
  | 'notification-settings'
  | 'report-guide'
  | 'report'
  | 'classification-method'
  | 'classification-criteria'
  | 'service-info'
  | 'terms'
  | 'privacy'
  | 'security-contact';

function getInitialTheme(): ThemeMode {
  const savedTheme = window.localStorage.getItem('theme-mode');

  if (savedTheme === 'light' || savedTheme === 'dark') {
    return savedTheme;
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

const pageContent: Record<
  'service-info' | 'terms' | 'privacy' | 'security-contact',
  { title: string; description: string }
> = {
  'service-info': {
    title: '서비스 소개',
    description: 'URL GUARD 서비스의 목적과 주요 기능을 소개하는 화면입니다.',
  },
  terms: {
    title: '이용약관',
    description: '서비스 이용 조건과 운영 정책을 확인할 수 있는 화면입니다.',
  },
  privacy: {
    title: '개인정보 처리방침',
    description: '개인정보 수집 및 이용 방침을 확인할 수 있는 화면입니다.',
  },
  'security-contact': {
    title: '보안 문의',
    description: '보안 관련 문의 및 신고 접수 채널을 안내하는 화면입니다.',
  },
};

function App() {
  const [theme, setTheme] = useState<ThemeMode>(getInitialTheme);
  const [view, setView] = useState<ViewMode>('dashboard');
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(Boolean(getAccessToken()));

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem('theme-mode', theme);
  }, [theme]);

  const refreshLoginState = () => {
    setIsLoggedIn(Boolean(getAccessToken()));
  };

  const handleNavigate = (nextView: ViewMode) => {
    refreshLoginState();
    setView(nextView);
  };

  const handleToggleTheme = () => {
    setTheme((currentTheme) => (currentTheme === 'light' ? 'dark' : 'light'));
  };

  const handleGoHome = () => {
    handleNavigate('dashboard');
  };

  const handleGoLogin = () => {
    handleNavigate('login');
  };

  const handleGoSignup = () => {
    handleNavigate('signup');
  };

  const handleGoMyPage = () => {
    handleNavigate('mypage');
  };

  const handleLogout = () => {
    clearTokens();
    setIsLoggedIn(false);
    setView('dashboard');
  };

  const sharedProps = {
    theme,
    onToggleTheme: handleToggleTheme,
    onGoHome: handleGoHome,
    onGoLogin: handleGoLogin,
    onGoSignup: handleGoSignup,
    onGoMyPage: handleGoMyPage,
  };

  const authProps = {
    isLoggedIn,
    onLogout: handleLogout,
    onNavigate: handleNavigate,
  };

  if (window.location.pathname === '/oauth/callback') {
    return (
      <OAuthCallbackPage
        onSuccess={() => {
          refreshLoginState();
          setView('dashboard');
        }}
        onFail={() => {
          clearTokens();
          setIsLoggedIn(false);
          setView('login');
        }}
      />
    );
  }

  if (view === 'login' || view === 'signup') {
    return <AuthPage {...sharedProps} mode={view} />;
  }

  if (view === 'dashboard') {
    return <Dashboard {...sharedProps} {...authProps} />;
  }

  if (view === 'mypage') {
    return <MyPage {...sharedProps} {...authProps} />;
  }

  if (view === 'my-mailbox' || view === 'mail-connect') {
    return <MailPage {...sharedProps} {...authProps} currentView={view} />;
  }

  if (view === 'my-url' || view === 'url-library') {
    return <UrlPage {...sharedProps} {...authProps} currentView={view} />;
  }

  if (view === 'notifications' || view === 'notification-settings') {
    return <NotificationPage {...sharedProps} {...authProps} currentView={view} />;
  }

  if (view === 'report-guide' || view === 'report') {
    return <ReportPage {...sharedProps} {...authProps} currentView={view} />;
  }

  if (view === 'classification-method' || view === 'classification-criteria') {
    return <ClassificationPage {...sharedProps} {...authProps} currentView={view} />;
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