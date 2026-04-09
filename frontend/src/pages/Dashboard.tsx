import ChartBox from '../components/ChartBox';
import Header from '../components/Header';
import Navbar from '../components/Navbar';
import SummaryCard from '../components/SummaryCard';
import '../styles/Dashboard.css';

type ThemeMode = 'light' | 'dark';

interface DashboardProps {
  theme: ThemeMode;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
}

const summaryCards = [
  {
    title: '오늘 차단된 위험 URL',
    value: '1,284건',
    change: '+12.8% 전일 대비',
    tone: 'critical' as const,
  },
  {
    title: '검토 완료된 신고',
    value: '326건',
    change: '평균 처리 14분',
    tone: 'steady' as const,
  },
  {
    title: '정상 분류 정확도',
    value: '98.4%',
    change: '+0.6% 최근 7일',
    tone: 'positive' as const,
  },
];

const totalCollection = [
  { name: '매우 위험', value: 742 },
  { name: '위험', value: 416 },
  { name: '주의', value: 2018 },
  { name: '안전', value: 3621 },
];

const todayCollection = [
  { name: '매우 위험', value: 124 },
  { name: '위험', value: 87 },
  { name: '주의', value: 315 },
  { name: '안전', value: 758 },
];

const timeline = [
  {
    time: '09:20',
    title: '의심 도메인 대량 유입 감지',
    detail: '메일함 연동 계정 3곳에서 동일 패턴 URL이 급증했습니다.',
  },
  {
    time: '10:05',
    title: '자동 차단 규칙 적용',
    detail: '고위험 시그니처 2종이 URL 수집 정책에 반영되었습니다.',
  },
  {
    time: '11:40',
    title: '신고 처리 SLA 정상화',
    detail: '수동 검토 큐가 안정권으로 내려와 평균 응답 시간이 개선됐습니다.',
  },
];

function Dashboard({
  theme,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
}: DashboardProps) {
  return (
    <div className="dashboard-shell">
      <Header
        currentView="dashboard"
        theme={theme}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onToggleTheme={onToggleTheme}
      />
      <Navbar />

      <main className="dashboard-main">
        <section className="hero-panel">
          <div className="hero-copy">
            <p className="eyebrow">Security operations overview</p>
            <h1>메일 기반 악성 URL을 빠르게 식별하고 즉시 대응하는 운영 대시보드</h1>
            <p className="hero-text">
              수집, 분류, 신고 처리, 위험도 추이를 한 흐름으로 연결해 운영팀이 필요한
              조치를 즉시 판단할 수 있도록 구성했습니다.
            </p>

            <div className="hero-actions">
              <button className="primary-button" type="button">
                실시간 현황 보기
              </button>
              <button className="secondary-button" type="button">
                주간 보고서 다운로드
              </button>
            </div>
          </div>

          <div className="status-panel">
            <div className="status-head">
              <p className="status-label">Current posture</p>
              <strong>주의 단계</strong>
            </div>
            <div className="status-meter">
              <div className="status-meter-fill" />
            </div>
            <div className="status-grid">
              <div>
                <span>차단 정책</span>
                <strong>활성 14개</strong>
              </div>
              <div>
                <span>처리 대기</span>
                <strong>23건</strong>
              </div>
              <div>
                <span>평균 탐지</span>
                <strong>1.8초</strong>
              </div>
              <div>
                <span>오탐률</span>
                <strong>0.9%</strong>
              </div>
            </div>
          </div>
        </section>

        <section className="summary-grid">
          {summaryCards.map((card) => (
            <SummaryCard
              key={card.title}
              title={card.title}
              value={card.value}
              change={card.change}
              tone={card.tone}
            />
          ))}
        </section>

        <section className="content-grid">
          <section className="overview-card">
            <div className="card-head">
              <div>
                <p className="eyebrow">Overview</p>
                <h2>운영 현황 요약</h2>
              </div>
              <span className="card-pill">Live</span>
            </div>

            <p className="overview-text">
              최근 24시간 동안 수집된 URL 중 고위험 비율이 상승하고 있습니다. 메일함
              연동 구간에서 유입된 링크가 대부분이며, 자동 차단 정책은 정상 작동 중입니다.
            </p>

            <div className="signal-list">
              <div>
                <span>가장 위험한 유입 채널</span>
                <strong>외부 메일 포워딩</strong>
              </div>
              <div>
                <span>집중 대응 권장 항목</span>
                <strong>유사 도메인 신고 자동화</strong>
              </div>
              <div>
                <span>다음 점검 시각</span>
                <strong>오늘 18:30</strong>
              </div>
            </div>

            <div className="timeline">
              {timeline.map((item) => (
                <article key={item.time} className="timeline-item">
                  <span>{item.time}</span>
                  <div>
                    <strong>{item.title}</strong>
                    <p>{item.detail}</p>
                  </div>
                </article>
              ))}
            </div>
          </section>

          <section className="charts-stack">
            <ChartBox
              title="전체 수집 URL"
              caption="누적 분류 기준"
              total="6,797건"
              data={totalCollection}
            />
            <ChartBox
              title="오늘 수집 URL"
              caption="금일 00:00 이후"
              total="1,284건"
              data={todayCollection}
            />
          </section>
        </section>
      </main>

      <footer className="footer">
        <span>서비스 소개</span>
        <span>이용약관</span>
        <span>개인정보 처리방침</span>
        <span>보안 문의</span>
      </footer>
    </div>
  );
}

export default Dashboard;
