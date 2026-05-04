import { useEffect, useState } from 'react';
import AuthPage from './pages/AuthPage';
import ClassificationPage from './pages/ClassificationPage';
import Dashboard from './pages/Dashboard';
import MailPage from './pages/MailPage';
import MyPage from './pages/MyPage';
import NotificationPage from './pages/NotificationPage';
import OAuthCallback from './pages/OAuthCallback';
import ReportPage from './pages/ReportPage';
import SimplePage from './pages/SimplePage';
import UrlPage from './pages/UrlPage';
import './styles/Dashboard.css';

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
  Exclude<
    ViewMode,
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
  >,
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

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    window.localStorage.setItem('theme-mode', theme);
  }, [theme]);

  const sharedProps = {
    theme,
    onToggleTheme: () =>
      setTheme((currentTheme) => (currentTheme === 'light' ? 'dark' : 'light')),
    onGoHome: () => setView('dashboard'),
    onGoLogin: () => setView('login'),
    onGoSignup: () => setView('signup'),
    onGoMyPage: () => setView('mypage'),
  };

  if (window.location.pathname === '/oauth/callback') {
    return <OAuthCallback onGoHome={() => setView('dashboard')} />;
  }

  if (view === 'login' || view === 'signup') {
    return <AuthPage {...sharedProps} mode={view} />;
  }

  if (view === 'dashboard') {
    return <Dashboard {...sharedProps} onNavigate={setView} />;
  }

  if (view === 'mypage') {
    return <MyPage {...sharedProps} onNavigate={setView} />;
  }

  if (view === 'my-mailbox' || view === 'mail-connect') {
    return <MailPage {...sharedProps} currentView={view} onNavigate={setView} />;
  }

  if (view === 'my-url' || view === 'url-library') {
    return <UrlPage {...sharedProps} currentView={view} onNavigate={setView} />;
  }

  if (view === 'notifications' || view === 'notification-settings') {
    return <NotificationPage {...sharedProps} currentView={view} onNavigate={setView} />;
  }

  if (view === 'report-guide' || view === 'report') {
    return <ReportPage {...sharedProps} currentView={view} onNavigate={setView} />;
  }

  if (view === 'classification-method' || view === 'classification-criteria') {
    return <ClassificationPage {...sharedProps} currentView={view} onNavigate={setView} />;
  }

  const currentPage = pageContent[view];

  return (
    <SimplePage
      {...sharedProps}
      currentView={view}
      title={currentPage.title}
      description={currentPage.description}
      onNavigate={setView}
    />
  );
}

export default App;