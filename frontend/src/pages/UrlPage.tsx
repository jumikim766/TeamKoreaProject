import { useMemo, useState } from 'react';
import ChartBox from '../components/ChartBox';
import Header from '../components/Header';
import Navbar from '../components/Navbar';
import '../styles/UrlPage.css';
import { getRiskClassName, getRiskLabel, type RiskLevelLabel } from '../utils/riskLevel';

type ThemeMode = 'light' | 'dark';
type UrlViewMode = 'url-statistics' | 'my-url' | 'url-library';

type PageViewTarget =
  | 'my-mailbox'
  | 'mail-connect'
  | 'url-statistics'
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

const PAGE_SIZE = 20;

const myUrlItems: UrlItem[] = Array.from({ length: 26 }, (_, index) => ({
  id: index + 1,
  sender: index % 2 === 0 ? '보안팀' : '알 수 없는 발신자',
  link:
    index % 3 === 0
      ? `http://danger-example-${index + 1}.com/login`
      : `https://safe-example-${index + 1}.com/document`,
  date: `03.${String(25 - (index % 10)).padStart(2, '0')}`,
  time: `${String(12 - (index % 5)).padStart(2, '0')}:34`,
  risk: index % 5 === 0 ? '심각' : index % 5 === 1 ? '위험' : index % 5 === 2 ? '주의' : index % 5 === 3 ? '의심' : '안전',
  reason: [
    'URL의 도메인 패턴과 접속 유도 방식이 분석되었습니다.',
    '메일 본문에서 사용자를 외부 페이지로 이동시키는 링크로 탐지되었습니다.',
    '접속 전 발신자, 도메인, 요청 내용을 확인하는 것이 좋습니다.',
  ],
}));

const urlLibraryItems: UrlItem[] = Array.from({ length: 33 }, (_, index) => ({
  id: index + 1,
  link:
    index % 4 === 0
      ? `http://public-danger-${index + 1}.com/verify`
      : `https://public-url-${index + 1}.com/info`,
  date: `03.${String(25 - (index % 12)).padStart(2, '0')}`,
  time: `${String(10 + (index % 8)).padStart(2, '0')}:21`,
  risk: index % 5 === 0 ? '심각' : index % 5 === 1 ? '위험' : index % 5 === 2 ? '주의' : index % 5 === 3 ? '의심' : '안전',
  reason: [
    '전체 회원의 링크 분석 데이터에서 수집된 URL입니다.',
    '동일하거나 유사한 패턴의 URL이 여러 번 탐지되었습니다.',
    '위험도는 URL 패턴, 신고 여부, 분석 결과를 기준으로 표시됩니다.',
  ],
}));

const chartData = [
  { name: '심각', value: 1234 },
  { name: '위험', value: 984 },
  { name: '주의', value: 765 },
  { name: '의심', value: 432 },
  { name: '안전', value: 3200 },
];

const pageInfo = {
  'url-statistics': {
    title: 'URL 통계',
    description:
      '내 이메일에 포함된 링크의 위험도를 분석하고, 전체 회원의 통계와 비교해 내 이메일 환경이 얼마나 안전한지 한눈에 확인하세요.',
  },
  'my-url': {
    title: '나의 URL',
    description: '내 이메일 링크 분석',
  },
  'url-library': {
    title: '전체 URL 모음',
    description: '전체 회원 링크 통계',
  },
};

function PlusIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M5 12h14" />
      <path d="M12 5v14" />
    </svg>
  );
}

function MinusIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M5 12h14" />
    </svg>
  );
}

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
  const [currentPage, setCurrentPage] = useState(1);

  const isStatistics = currentView === 'url-statistics';
  const isMyUrl = currentView === 'my-url';

  const urlItems = useMemo(() => {
    const items = isMyUrl ? myUrlItems : urlLibraryItems;
    return [...items].sort((a, b) => b.id - a.id);
  }, [isMyUrl]);

  const totalPages = Math.ceil(urlItems.length / PAGE_SIZE);

  const pagedUrlItems = useMemo(() => {
    const startIndex = (currentPage - 1) * PAGE_SIZE;
    return urlItems.slice(startIndex, startIndex + PAGE_SIZE);
  }, [urlItems, currentPage]);

  const handleChangeMenu = (view: UrlViewMode) => {
    setCurrentPage(1);
    setOpenedUrlId(null);
    onNavigate(view);
  };

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

              <button className={currentView === 'url-statistics' ? 'side-menu-button is-active' : 'side-menu-button'} onClick={() => handleChangeMenu('url-statistics')} type="button">
                URL 통계
              </button>

              <button className={currentView === 'my-url' ? 'side-menu-button is-active' : 'side-menu-button'} onClick={() => handleChangeMenu('my-url')} type="button">
                나의 URL
              </button>

              <button className={currentView === 'url-library' ? 'side-menu-button is-active' : 'side-menu-button'} onClick={() => handleChangeMenu('url-library')} type="button">
                전체 URL 모음
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            <div className="url-section">
              <div className="url-page-title-box">
                <p className="eyebrow">URL Guard</p>
                <h1>{pageInfo[currentView].title}</h1>
                <p>{pageInfo[currentView].description}</p>
              </div>

              {isStatistics ? (
                <section className="url-stat-grid">
                  <div className="url-stat-card">
                    <span>내 이메일 전체 링크</span>
                    <strong>1,248개</strong>
                    <p>연동된 이메일에서 탐지된 전체 링크 수입니다.</p>
                  </div>

                  <div className="url-stat-card">
                    <span>고위험 링크</span>
                    <strong>42개</strong>
                    <p>위험 또는 심각으로 분류된 링크 수입니다.</p>
                  </div>

                  <div className="url-stat-card">
  <span>전체 회원 전체 링크</span>
  <strong>6,615개</strong>
  <p>전체 회원의 이메일과 URL 분석에서 탐지된 전체 링크 수입니다.</p>
</div>

                  <div className="url-stat-chart">
                    <ChartBox title="내 URL 위험도 통계" caption="이메일 링크 분석 기준" total="1,248개" data={chartData} />
                  </div>
                  <div className="url-stat-chart">
  <ChartBox
    title="전체 URL 위험도 통계"
    caption="전체 회원 링크 분석 기준"
    total="6,615개"
    data={chartData}
  />
</div>
                </section>
              ) : (
                <>
                  {isMyUrl && (
                    <div className="url-top-bar">
                      <select className="url-filter-select" value={selectedAccount} onChange={(event) => setSelectedAccount(event.target.value)}>
                        <option value="1234@5678.com">1234@5678.com</option>
                        <option value="8765@4321.com">8765@4321.com</option>
                        <option value="abcd@efgh.com">abcd@efgh.com</option>
                      </select>
                    </div>
                  )}

                  <section className="url-list-card">
                    <div className="url-list-head">
                      <div>
                        <h2 className="url-list-title">{pageInfo[currentView].title}</h2>
                        <p className="url-list-count">
                          총 <strong>{urlItems.length}</strong>건 · 최신 링크가 위에 표시됩니다.
                        </p>
                      </div>
                    </div>

                    <div className={isMyUrl ? 'url-table-grid url-table-header-row' : 'url-table-grid url-library-table-grid url-table-header-row'}>
                      <span>번호</span>
                      {isMyUrl && <span>보낸 사람</span>}
                      <span>URL</span>
                      <span>검사일시</span>
                      <span>위험도</span>
                      <span>설명</span>
                    </div>

                    <div className="url-table-body">
                      {pagedUrlItems.map((item, index) => {
                        const isOpened = openedUrlId === item.id;
                        const displayNumber = urlItems.length - ((currentPage - 1) * PAGE_SIZE + index);

                        return (
                          <div className="url-row-block" key={item.id}>
                            <div className={isMyUrl ? 'url-table-grid url-table-data-row' : 'url-table-grid url-library-table-grid url-table-data-row'}>
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

                              <button className="url-detail-toggle" onClick={() => handleToggleReason(item.id)} type="button" aria-label={isOpened ? 'URL 설명 닫기' : 'URL 설명 열기'}>
                                {isOpened ? <MinusIcon /> : <PlusIcon />}
                              </button>
                            </div>

                            {isOpened && (
                              <div className="url-risk-reason-box">
                                <strong>위험 분석 사유</strong>
                                {item.reason.map((reason) => (
                                  <p key={reason}>· {reason}</p>
                                ))}
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>

                    {totalPages > 1 && (
                      <div className="url-pagination">
                        {Array.from({ length: totalPages }, (_, index) => (
                          <button key={index + 1} className={currentPage === index + 1 ? 'is-active' : ''} onClick={() => setCurrentPage(index + 1)} type="button">
                            {index + 1}
                          </button>
                        ))}
                      </div>
                    )}
                  </section>
                </>
              )}
            </div>
          </section>
        </div>
      </main>

      <footer className="footer">
        <button type="button" onClick={() => onNavigate('service-info')}>서비스 소개</button>
        <button type="button" onClick={() => onNavigate('terms')}>이용약관</button>
        <button type="button" onClick={() => onNavigate('privacy')}>개인정보 처리방침</button>
        <button type="button" onClick={() => onNavigate('security-contact')}>보안 문의</button>
      </footer>
    </div>
  );
}

export default UrlPage;