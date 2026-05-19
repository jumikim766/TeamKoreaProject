import { useMemo, useState } from 'react';
import ChartBox from '../components/ChartBox';
import Header from '../components/Header';
import Navbar from '../components/Navbar';
import "../styles/UrlPage.css";
import { getRiskClassName, getRiskLabel, type RiskLevelLabel } from '../utils/riskLevel';

type ThemeMode = 'light' | 'dark';
type UrlViewMode = 'my-url' | 'url-library';

type PageViewTarget =
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

interface UrlItem {
  id: number;
  sender?: string;
  link: string;
  date: string;
  time: string;
  risk: RiskLevelLabel;
  reason: string[];
}

interface UrlPageProps {
  theme: ThemeMode;
  currentView: UrlViewMode;
  isLoggedIn?: boolean;
  userName?: string;
  onLogout?: () => void;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
  onNavigate: (view: PageViewTarget) => void;
}

const myUrlItems: UrlItem[] = [
  {
    id: 1,
    sender: 'XXX',
    link: 'http://www.xxxyyyzzz.com/@@@###$$$%%%',
    date: '03.25',
    time: '12:34',
    risk: '심각',
    reason: [
      '해당 URL은 피싱 위험이 높은 주소 패턴으로 분석되었습니다.',
      '비정상적으로 긴 특수문자 조합이 포함되어 있습니다.',
      '사용자 정보 입력을 유도할 가능성이 있습니다.',
      '접속 전 링크 출처를 반드시 확인해야 합니다.',
    ],
  },
  {
    id: 2,
    sender: '보안팀',
    link: 'https://safe-example.com/document',
    date: '03.25',
    time: '10:20',
    risk: '안전',
    reason: [
      '정상적인 HTTPS 연결을 사용하는 URL입니다.',
      '현재까지 악성코드 유포 이력이나 피싱 신고 이력이 발견되지 않았습니다.',
      '다만 외부 링크 접속 시에는 항상 도메인을 확인하는 것이 좋습니다.',
    ],
  },
];

const urlLibraryItems: UrlItem[] = [
  {
    id: 1,
    link: 'http://www.xxxyyyzzz.com/@@@###$$$%%%',
    date: '03.25',
    time: '12:34',
    risk: '심각',
    reason: [
      '여러 사용자에게 반복적으로 탐지된 고위험 URL입니다.',
      '피싱 사이트와 유사한 도메인 패턴을 사용하고 있습니다.',
      '개인정보 탈취 시도 가능성이 있어 접속을 피하는 것이 좋습니다.',
    ],
  },
  {
    id: 2,
    link: 'https://warning-example.com/event',
    date: '03.24',
    time: '09:21',
    risk: '주의',
    reason: [
      'URL 자체는 접속 가능하지만 출처 신뢰도가 낮습니다.',
      '이벤트, 쿠폰, 로그인 유도 문구가 포함된 페이지로 연결될 수 있습니다.',
      '접속 전 발신자와 도메인을 확인하는 것이 필요합니다.',
    ],
  },
];

const chartData = [
  { name: '심각', value: 1234 },
  { name: '위험', value: 1234 },
  { name: '주의', value: 1234 },
  { name: '의심', value: 1234 },
  { name: '안전', value: 1234 },
];

function UrlPage({
  theme,
  currentView,
  isLoggedIn = false,
  userName = '팀코',
  onLogout,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
}: UrlPageProps) {
  const [selectedAccount, setSelectedAccount] = useState('1234@5678.com');
  const [openedUrlId, setOpenedUrlId] = useState<number | null>(null);

  const isMyUrl = currentView === 'my-url';

  const urlItems = useMemo(() => {
    const items = isMyUrl ? myUrlItems : urlLibraryItems;

    return [...items].sort((a, b) => b.id - a.id);
  }, [isMyUrl]);

  const handleToggleReason = (id: number) => {
    setOpenedUrlId((prevId) => (prevId === id ? null : id));
  };

  return (
    <div className="dashboard-shell">
      <Header
        currentView={currentView}
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

      <main className="page-main">
        <div className="page-layout">
          <aside className="page-sidebar">
            <button className="back-button" onClick={onGoHome} type="button">
              뒤로가기
            </button>

            <div className="page-side-card">
              <div className="page-side-title">URL 관리</div>

              <button
                className={currentView === 'my-url' ? 'side-menu-button is-active' : 'side-menu-button'}
                onClick={() => onNavigate('my-url')}
                type="button"
              >
                나의 URL
              </button>

              <button
                className={currentView === 'url-library' ? 'side-menu-button is-active' : 'side-menu-button'}
                onClick={() => onNavigate('url-library')}
                type="button"
              >
                URL 모음
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            <div className="url-section">
              {isMyUrl && (
                <div className="url-top-bar">
                  <select
                    className="url-filter-select"
                    value={selectedAccount}
                    onChange={(event) => setSelectedAccount(event.target.value)}
                  >
                    <option value="1234@5678.com">1234@5678.com</option>
                    <option value="8765@4321.com">8765@4321.com</option>
                    <option value="abcd@efgh.com">abcd@efgh.com</option>
                  </select>
                </div>
              )}

              <section className="url-overview-card">
                <div className="url-overview-copy">
                  <p className="eyebrow">{isMyUrl ? 'My URL history' : 'URL library'}</p>
                  <h1>{isMyUrl ? '내가 받은 URL' : '전체 탐지 URL'}</h1>
                  <strong>{urlItems.length.toLocaleString()} 개</strong>
                </div>

                <div className="url-overview-chart">
                  <ChartBox
                    title="URL 위험도"
                    caption="현재 분류 기준"
                    total="1,234,567개"
                    data={chartData}
                  />
                </div>
              </section>

              <section className="url-list-card url-list-card">
                <div className="url-list-head">
                  <div>
                    <h2 className="url-list-title">
                      {isMyUrl ? '나의 URL' : 'URL 모음'}
                    </h2>
                    <p className="url-list-count">
                      총 <strong>{urlItems.length}</strong>건 · 최신 링크가 위에 표시됩니다.
                    </p>
                  </div>
                </div>

                <div
                  className={
                    isMyUrl
                      ? 'url-table-grid url-table-header-row'
                      : 'url-table-grid url-library-table-grid url-table-header-row'
                  }
                >
                  <span>번호</span>
                  {isMyUrl && <span>보낸 사람</span>}
                  <span>URL</span>
                  <span>검사일시</span>
                  <span>위험도</span>
                  <span>설명</span>
                </div>

                <div className="url-table-body">
                  {urlItems.map((item, index) => {
                    const isOpened = openedUrlId === item.id;
                    const displayNumber = urlItems.length - index;

                    return (
                      <div className="url-row-block" key={item.id}>
                        <div
                          className={
                            isMyUrl
                              ? 'url-table-grid url-table-data-row'
                              : 'url-table-grid url-library-table-grid url-table-data-row'
                          }
                        >
                          <span className="url-number-text">{displayNumber}</span>

                          {isMyUrl && (
                            <span>
                              <strong>{item.sender}</strong>
                            </span>
                          )}

                          <span className="url-link-text">{item.link}</span>

                          <span>
                            {item.date} {item.time}
                          </span>

                          <span>
                            <span className={`risk-badge ${getRiskClassName(item.risk)}`}>
                              {getRiskLabel(item.risk)}
                            </span>
                          </span>

                          <button
                            className="url-detail-toggle"
                            onClick={() => handleToggleReason(item.id)}
                            type="button"
                            aria-label={isOpened ? 'URL 설명 닫기' : 'URL 설명 열기'}
                          >
                            {isOpened ? '−' : '+'}
                          </button>
                        </div>

                        {isOpened && (
                          <div className="url-risk-reason-box">
                            {item.reason.map((reason) => (
                              <p key={reason}>· {reason}</p>
                            ))}
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              </section>
            </div>
          </section>
        </div>
      </main>

      <footer className="footer">
        <button type="button" onClick={() => onNavigate('service-info')}>
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

export default UrlPage;