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

interface UrlRow {
  id: number;
  account?: string;
  sender: string;
  url: string;
  date: string;
  risk: '매우 위험' | '위험' | '주의' | '안전';
}

interface UrlPageProps {
  theme: ThemeMode;
  currentView: UrlViewMode;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
  onNavigate: (view: PageViewTarget) => void;
}

const accounts = ['1234@5678.com', '8765@4321.com', 'abcd@efgh.com'];

const collectionData = [
  { name: '매우 위험', value: 742 },
  { name: '위험', value: 416 },
  { name: '주의', value: 2018 },
  { name: '안전', value: 3621 },
];

const myUrlRows: UrlRow[] = [
  {
    id: 1,
    account: '1234@5678.com',
    sender: 'XXX',
    url: 'http://www.xxxyyyzzz.com/@@@##$$$%%',
    date: '03.25 12:34',
    risk: '매우 위험',
  },
  {
    id: 2,
    account: '1234@5678.com',
    sender: '보안팀',
    url: 'https://alert-security-check.net/login',
    date: '03.25 10:20',
    risk: '위험',
  },
  {
    id: 3,
    account: '8765@4321.com',
    sender: '관리자',
    url: 'https://safe-company-link.com/report',
    date: '03.24 16:02',
    risk: '안전',
  },
];

const allUrlRows: UrlRow[] = [
  {
    id: 11,
    sender: '-',
    url: 'http://www.xxxyyyzzz.com/@@@##$$$%%',
    date: '03.25 12:34',
    risk: '매우 위험',
  },
  {
    id: 12,
    sender: '-',
    url: 'https://danger-login-check.site/reset',
    date: '03.25 11:08',
    risk: '위험',
  },
  {
    id: 13,
    sender: '-',
    url: 'https://notice-verify.company-mail.help',
    date: '03.25 09:40',
    risk: '주의',
  },
];

function UrlPage({
  theme,
  currentView,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
}: UrlPageProps) {
  const selectedAccount = accounts[0];
  const filteredMyRows = myUrlRows.filter((row) => row.account === selectedAccount);

  return (
    <div className="dashboard-shell">
      <Header
        currentView={currentView}
        theme={theme}
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
            <div className="url-section">
              {currentView === 'my-url' ? (
                <>
                  <div className="mail-top-bar">
<select className="mail-account-select" value={selectedAccount} onChange={() => {}}>
                      {accounts.map((email) => (
                        <option key={email} value={email}>
                          {email}
                        </option>
                      ))}
                    </select>
                  </div>

                  <section className="url-summary-card">
                    <div className="url-summary-copy">
                      <p className="eyebrow">My collected URLs</p>
                      <h2>내가 받은 URL</h2>
                      <strong>1,234,567개</strong>
                    </div>

                    <div className="url-summary-chart">
                      <ChartBox
                        title="나의 URL"
                        caption="받은 URL 기준"
                        total="1,234,567개"
                        data={collectionData}
                      />
                    </div>
                  </section>

                  <section className="url-table-card">
                    <div className="url-table-head">
                      <p className="eyebrow">Received URL list</p>
                      <h2>내가 받은 URL 목록</h2>
                    </div>

                    <div className="url-table url-table-header">
                      <span>보낸 사람</span>
                      <span>URL 링크</span>
                      <span>날짜 / 시간</span>
                      <span>위험도</span>
                    </div>

                    <div className="url-table-body">
                      {filteredMyRows.map((row) => (
                        <div key={row.id} className="url-table-row">
                          <span>{row.sender}</span>
                          <span className="url-link-text">{row.url}</span>
                          <span>{row.date}</span>
                          <span>
                            <strong className={`risk-badge risk-${row.risk}`}>{row.risk}</strong>
                          </span>
                        </div>
                      ))}
                    </div>
                  </section>
                </>
              ) : (
                <>
                  <section className="url-summary-card">
                    <div className="url-summary-copy">
                      <p className="eyebrow">All detected URLs</p>
                      <h2>전체 탐지 URL</h2>
                      <strong>1,234,567개</strong>
                    </div>

                    <div className="url-summary-chart">
                      <ChartBox
                        title="전체 탐지 URL"
                        caption="누적 탐지 기준"
                        total="1,234,567개"
                        data={collectionData}
                      />
                    </div>
                  </section>

                  <section className="url-table-card">
                    <div className="url-table-head">
                      <p className="eyebrow">Detected URL list</p>
                      <h2>전체 탐지 URL 목록</h2>
                    </div>

                    <div className="url-table url-table-header url-library-grid">
                      <span>URL 링크</span>
                      <span>날짜 / 시간</span>
                      <span>위험도</span>
                    </div>

                    <div className="url-table-body">
                      {allUrlRows.map((row) => (
                        <div key={row.id} className="url-table-row url-library-grid">
                          <span className="url-link-text">{row.url}</span>
                          <span>{row.date}</span>
                          <span>
                            <strong className={`risk-badge risk-${row.risk}`}>{row.risk}</strong>
                          </span>
                        </div>
                      ))}
                    </div>
                  </section>
                </>
              )}
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