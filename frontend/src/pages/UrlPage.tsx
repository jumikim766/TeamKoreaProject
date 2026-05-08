import { useMemo, useState } from 'react';
import ChartBox from '../components/ChartBox';
import Header from '../components/Header';
import '../styles/Dashboard.css';

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
  risk: '매우 위험' | '위험' | '주의' | '안전';
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
    risk: '매우 위험',
  },
  {
    id: 2,
    sender: '보안팀',
    link: 'https://safe-example.com/document',
    date: '03.25',
    time: '10:20',
    risk: '안전',
  },
];

const urlLibraryItems: UrlItem[] = [
  {
    id: 1,
    link: 'http://www.xxxyyyzzz.com/@@@###$$$%%%',
    date: '03.25',
    time: '12:34',
    risk: '매우 위험',
  },
  {
    id: 2,
    link: 'https://warning-example.com/event',
    date: '03.24',
    time: '09:21',
    risk: '주의',
  },
];

const chartData = [
  { name: '매우 위험', value: 1234 },
  { name: '위험', value: 1234 },
  { name: '주의', value: 1234 },
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

  const isMyUrl = currentView === 'my-url';

  const urlItems = useMemo(() => {
    return isMyUrl ? myUrlItems : urlLibraryItems;
  }, [isMyUrl]);

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
                className={
                  currentView === 'url-library' ? 'side-menu-button is-active' : 'side-menu-button'
                }
                onClick={() => onNavigate('url-library')}
                type="button"
              >
                URL 모음
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            <div className="mail-section">
              {isMyUrl && (
                <div className="mail-top-bar">
                  <select
                    className="mail-account-select"
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
                  <p className="eyebrow">
                  </p>
                  <h1>{isMyUrl ? '내가 받은 URL' : '전체 탐지 URL'}</h1>
                  <strong>1,234,567 개</strong>
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

              <section className="mail-list-card url-list-card">
                <h2 className="url-list-title">
                  {isMyUrl ? '내가 받은 URL 목록' : '전체 탐지 URL 목록'}
                </h2>

                <div
                  className={
                    isMyUrl
                      ? 'url-table-grid url-table-header-row'
                      : 'url-table-grid url-library-table-grid url-table-header-row'
                  }
                >
                  {isMyUrl && <span>보낸 사람</span>}
                  <span>URL 링크</span>
                  <span>날짜 / 시간</span>
                  <span>위험도</span>
                </div>

                <div className="mail-table-body">
                  {urlItems.map((item) => (
                    <div
                      key={item.id}
                      className={
                        isMyUrl
                          ? 'url-table-grid url-table-data-row'
                          : 'url-table-grid url-library-table-grid url-table-data-row'
                      }
                    >
                      {isMyUrl && (
                        <span>
                          <strong>{item.sender}</strong>
                        </span>
                      )}

                      <span className="url-link-text">{item.link}</span>

                      <span>
                        {item.date} {item.time}
                      </span>

                      <span className={`risk-badge risk-${item.risk}`}>
                        {item.risk}
                      </span>
                    </div>
                  ))}
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