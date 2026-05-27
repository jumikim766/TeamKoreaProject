import { useEffect, useState } from 'react';
import ChartBox from '../components/ChartBox';
import Header from '../components/Header';
import Navbar from '../components/Navbar';
import "../styles/UrlPage.css";
import { getRiskClassName, getRiskLabel, type RiskLevelLabel } from '../utils/riskLevel';
import {
  getMyUrls,
  analyzeUrlWithLlm,
  type MyUrlItem,
  type LlmAnalysisResponse,
} from '../api/urlApi';

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
  const [urlItems, setUrlItems] = useState<MyUrlItem[]>([]);
  const [llmResult, setLlmResult] = useState<LlmAnalysisResponse | null>(null);
  const [analyzingUrlId, setAnalyzingUrlId] = useState<number | null>(null);
  const [selectedLlmUrlId, setSelectedLlmUrlId] = useState<number | null>(null);

  const isMyUrl = currentView === 'my-url';

  useEffect(() => {
    const fetchUrls = async () => {
      try {
        const response = await getMyUrls({
          page: 0,
          size: 20,
        });

        setUrlItems(response.urls);
      } catch (error) {
        console.error(error);
      }
    };

    fetchUrls();
  }, []);

  const handleToggleReason = (id: number) => {
    setOpenedUrlId((prevId) => (prevId === id ? null : id));
  };

  const handleLlmAnalyze = async (urlId: number) => {
    try {
      setAnalyzingUrlId(urlId);

      const result = await analyzeUrlWithLlm(urlId);

      setLlmResult(result);
      setSelectedLlmUrlId(urlId);

      alert('LLM 분석이 완료되었습니다.');
    } catch (error) {
      console.error(error);
      alert('LLM 분석 중 오류가 발생했습니다.');
    } finally {
      setAnalyzingUrlId(null);
    }
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
                    const isOpened = openedUrlId === item.urlId;
                    const displayNumber = urlItems.length - index;

                    return (
                      <div className="url-row-block" key={item.urlId}>
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
                              <strong>{item.senderName ?? '알 수 없음'}</strong>
                            </span>
                          )}

                          <span className="url-link-text">{item.normalizedUrl}</span>

                          <span>
                            {new Date(item.createdAt).toLocaleString()}
                          </span>

                          <span>
                            <span
                              className={`risk-badge ${getRiskClassName(
                                item.riskLevel as RiskLevelLabel
                              )}`}
                            >
                              {getRiskLabel(item.riskLevel as RiskLevelLabel)}
                            </span>
                          </span>

                          <button
                            className="url-detail-toggle"
                            onClick={() => handleToggleReason(item.urlId)}
                            type="button"
                            aria-label={isOpened ? 'URL 설명 닫기' : 'URL 설명 열기'}
                          >
                            {isOpened ? '−' : '+'}
                          </button>

                          <button
                            className="url-detail-toggle"
                            onClick={() => handleLlmAnalyze(item.urlId)}
                            type="button"
                          >
                            {analyzingUrlId === item.urlId ? '분석 중...' : 'LLM 분석'}
                          </button>
                        </div>

                        {isOpened && (
                          <div className="url-risk-reason-box">
                            <p>· {item.reasonSummary ?? '분석 설명이 없습니다.'}</p>
                          </div>
                        )}

                        {llmResult && selectedLlmUrlId === item.urlId && (
                          <div className="url-risk-reason-box">
                            <p>위험도: {llmResult.risk}</p>
                            <p>점수: {llmResult.score}</p>
                            <p>{llmResult.reasonSummary}</p>

                            {llmResult.detectedRules.map((rule) => (
                              <p key={rule}>• {rule}</p>
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