import { useEffect, useState } from 'react';
import AuthPage from './pages/AuthPage';
import ClassificationPage from './pages/ClassificationPage';
import Dashboard from './pages/Dashboard';
import MailPage from './pages/MailPage';
import MyPage from './pages/MyPage';
import NotificationPage from './pages/NotificationPage';
import OAuthCallbackPage from './pages/OAuthCallbackPage';
import ReportPage from './pages/ReportPage';
import GuidePage from './pages/GuidePage';
import SimplePage from './pages/SimplePage';
import UrlPage from './pages/UrlPage';
import './styles/Dashboard.css';
import { clearTokens, getAccessToken } from './utils/token';


type ThemeMode = 'light' | 'dark';

export type ViewMode =
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
  | 'security-contact'
  | 'guide';

function getInitialTheme(): ThemeMode {
  const savedTheme = window.localStorage.getItem('theme-mode');

  if (savedTheme === 'light' || savedTheme === 'dark') {
    return savedTheme;
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function getSavedUserName() {
  return (
    window.localStorage.getItem('userName') ||
    window.localStorage.getItem('name') ||
    window.localStorage.getItem('nickname') ||
    '사용자'
  );
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
  const [userName, setUserName] = useState<string>(getSavedUserName);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem('theme-mode', theme);
  }, [theme]);

  const refreshLoginState = () => {
    const hasToken = Boolean(getAccessToken());

    setIsLoggedIn(hasToken);
    setUserName(hasToken ? getSavedUserName() : '사용자');
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
    setUserName('사용자');
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
    userName,
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
          setUserName('사용자');
          setView('login');
        }}
      />
    );
  }

  if (view === 'login' || view === 'signup') {
    return (
      <AuthPage
        {...sharedProps}
        {...authProps}
        mode={view}
        onLoginSuccess={() => {
          refreshLoginState();
          setView('dashboard');
        }}
      />
    );
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

  if (view === 'guide') {
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