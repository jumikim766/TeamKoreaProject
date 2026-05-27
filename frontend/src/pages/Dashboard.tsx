import ChartBox from '../components/ChartBox';
import Header from '../components/Header';
import Navbar from '../components/Navbar';
import '../styles/Dashboard.css';
import type { ViewMode } from '../App';
import { getUrlStatistics, type UrlStatistics } from '../api/urlApi';
import { useEffect, useState } from 'react';

type ThemeMode = 'light' | 'dark';



interface DashboardProps {
  theme: ThemeMode;
  isLoggedIn: boolean;
  onLogout: () => void;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
  onNavigate: (view: ViewMode) => void;
}

function Dashboard({
  theme,
  isLoggedIn,
  onLogout,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
}: DashboardProps) {
  
  const [allStats, setAllStats] = useState<UrlStatistics | null>(null);
  const [todayStats, setTodayStats] = useState<UrlStatistics | null>(null);

  useEffect(() => {
    const fetchDashboardStatistics = async () => {
      try {
        const [all, today] = await Promise.all([
          getUrlStatistics({ scope: 'ALL', period: 'ALL' }),
          getUrlStatistics({ scope: 'ALL', period: 'TODAY' }),
        ]);

        setAllStats(all);
        setTodayStats(today);
      } catch (error) {
        console.error('대시보드 URL 통계 조회 실패:', error);
      }
    };

    fetchDashboardStatistics();
  }, []);

  const totalCollection = [
    { name: '심각', value: allStats?.criticalCount ?? 0 },
    { name: '위험', value: allStats?.dangerCount ?? 0 },
    { name: '주의', value: allStats?.warningCount ?? 0 },
    { name: '의심', value: allStats?.suspiciousCount ?? 0 },
    { name: '안전', value: allStats?.safeCount ?? 0 },
  ];

  const todayCollection = [
    { name: '심각', value: todayStats?.criticalCount ?? 0 },
    { name: '위험', value: todayStats?.dangerCount ?? 0 },
    { name: '주의', value: todayStats?.warningCount ?? 0 },
    { name: '의심', value: todayStats?.suspiciousCount ?? 0 },
    { name: '안전', value: todayStats?.safeCount ?? 0 },
  ];

  const totalUrlCount = allStats?.totalCount ?? 0;
  const todayUrlCount = todayStats?.totalCount ?? 0;

  return (
    <div className={`dashboard-shell ${theme}`}>
      <Header
        currentView="dashboard"
        theme={theme}
        isLoggedIn={isLoggedIn}
        onLogout={onLogout}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onGoMyPage={onGoMyPage}
        onToggleTheme={onToggleTheme}
      />

      <Navbar onNavigate={onNavigate} />

      <main className="dashboard-main simple-dashboard-main">
        <section className="simple-main-board">
          <section className="simple-hero-panel">
            <div className="simple-hero-copy">
              <p className="eyebrow"></p>
              <h1>메일 기반 악성 URL을 빠르게 식별하고 즉시 대응하는 운영 대시보드</h1>
              <p className="hero-text">
                수집된 URL을 한눈에 확인할 수 있도록 핵심 정보만 단순하게 정리했습니다.
                운영자는 메일 기반 위협 흐름을 빠르게 파악하고 필요한 메뉴로 바로 이동할 수
                있습니다.
              </p>
            </div>
          </section>

          <section className="simple-chart-panel">
            <ChartBox
              title="총 수집 URL"
              caption="누적 분류 기준"
              total={`${totalUrlCount.toLocaleString()}건`}
              data={totalCollection}
            />

            <ChartBox
              title="오늘 수집 URL"
              caption="금일 00:00 이후"
              total={`${todayUrlCount.toLocaleString()}건`}
              data={todayCollection}
            />
          </section>
        </section>
      </main>

      <footer className="footer">
        <button type="button" onClick={() => onNavigate('guide')}>
          서비스 소개
        </button>
        <button type="button" onClick={() => onNavigate('terms')}>
          이용약관
        </button>
        <button type="button" onClick={() => onNavigate('privacy')}>
          개인정보 처리방침
        </button>
        <button type="button" onClick={() => onNavigate('security-contact')}>
          보안 문의
        </button>
      </footer>
    </div>
  );
}

export default Dashboard;