import { useEffect, useState } from 'react';
import AuthPage from './pages/AuthPage';
import Dashboard from './pages/Dashboard';
import SimplePage from './pages/SimplePage';
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
  Exclude<ViewMode, 'dashboard' | 'login' | 'signup'>,
  { title: string; description: string }
> = {
  mypage: {
    title: '마이페이지',
    description: '내 계정 정보와 개인 설정을 확인하고 관리할 수 있는 화면입니다.',
  },
  'my-mailbox': {
    title: '나의 메일함',
    description: '수집된 메일 내역과 메일 기반 위협 흐름을 확인할 수 있는 화면입니다.',
  },
  'mail-connect': {
    title: '메일 연동',
    description: '외부 메일 계정 연동 상태와 수집 설정을 관리하는 화면입니다.',
  },
  'my-url': {
    title: '나의 URL',
    description: '내가 관리 중인 URL 목록과 상태를 확인할 수 있는 화면입니다.',
  },
  'url-library': {
    title: 'URL 모음',
    description: '수집된 URL 데이터를 분류별로 모아볼 수 있는 화면입니다.',
  },
  notifications: {
    title: '알림함',
    description: '위협 탐지 및 처리 관련 알림을 한곳에서 확인할 수 있는 화면입니다.',
  },
  'notification-settings': {
    title: '알림 설정',
    description: '알림 수신 기준과 방식, 빈도를 설정할 수 있는 화면입니다.',
  },
  'report-guide': {
    title: '신고 안내',
    description: '위협 URL 신고 절차와 작성 가이드를 안내하는 화면입니다.',
  },
  report: {
    title: '신고하기',
    description: '위협 URL이나 의심 링크를 직접 신고할 수 있는 화면입니다.',
  },
  'classification-method': {
    title: '분류 방법',
    description: 'URL 분류 로직과 처리 흐름을 설명하는 화면입니다.',
  },
  'classification-criteria': {
    title: '분류기준',
    description: '위험도와 탐지 기준을 확인할 수 있는 화면입니다.',
  },
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

  if (view === 'login' || view === 'signup') {
    return <AuthPage {...sharedProps} mode={view} />;
  }

  if (view === 'dashboard') {
    return <Dashboard {...sharedProps} onNavigate={setView} />;
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