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
  userName?: string;
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
  userName = "사용자",
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
    if (!isLoggedIn) {
      setAllStats(null);
      setTodayStats(null);
      return;
    }

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
  }, [isLoggedIn]);

  const totalCollection = [
    { name: '위험', value: allStats?.dangerCount ?? 0 },
    { name: '주의', value: allStats?.warningCount ?? 0 },
    { name: '안전', value: allStats?.safeCount ?? 0 },
  ];

  const todayCollection = [
{ name: '위험', value: allStats?.dangerCount ?? 0 },
    { name: '주의', value: allStats?.warningCount ?? 0 },
    { name: '안전', value: allStats?.safeCount ?? 0 },
  ];

  const totalUrlCount = allStats?.totalCount ?? 0;
  const todayUrlCount = todayStats?.totalCount ?? 0;

  return (
    <div className={`dashboard-shell ${theme}`}>
      <Header
        currentView="dashboard"
        theme={theme}
        isLoggedIn={isLoggedIn}
        userName={userName}
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
              <p className="eyebrow">URL GUARD SERVICE</p>

              <h1>메일 속 위험 링크를 한눈에 확인하는 URL 보안 서비스</h1>

              <p className="hero-text">
                URL GUARD는 사용자의 메일에서 URL을 수집하고, 위험도를 분석해
                안전한 링크 사용을 도와주는 보안 플랫폼입니다.
              </p>

              <div className="service-card-grid">
                <article className="service-card">
                  <span className="service-icon">✉️</span>
                  <h3>메일 URL 수집</h3>
                  <p>연동된 메일함에서 URL을 자동으로 수집합니다.</p>
                </article>

                <article className="service-card">
                  <span className="service-icon">🔍</span>
                  <h3>위험도 분석</h3>
                  <p>수집된 URL을 안전, 주의, 위험, 3단계로 분류합니다.</p>
                </article>

                <article className="service-card">
                  <span className="service-icon">🔔</span>
                  <h3>위험 알림</h3>
                  <p>위험한 URL이 발견되면 알림함에서 바로 확인할 수 있습니다.</p>
                </article>

                <article className="service-card">
                  <span className="service-icon">📊</span>
                  <h3>통계 확인</h3>
                  <p>내 URL과 전체 URL 통계를 한눈에 확인할 수 있습니다.</p>
                </article>
              </div>
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