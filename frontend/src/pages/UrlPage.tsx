import { useEffect, useState } from "react";
import ChartBox from "../components/ChartBox";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import "../styles/UrlPage.css";
import {
  getRiskClassName,
  getRiskLabel,
  type RiskLevelLabel,
} from "../utils/riskLevel";
import {
  getMyUrls,
  type MyUrlItem,
} from "../api/urlApi";

type ThemeMode = "light" | "dark";
type UrlViewMode = "url-statistics" | "my-url" | "url-library";

type PageViewTarget =
  | "my-mailbox"
  | "mail-connect"
  | "url-statistics"
  | "my-url"
  | "url-library"
  | "notifications"
  | "notification-settings"
  | "report-guide"
  | "report"
  | "classification-method"
  | "classification-criteria"
  | "service-info"
  | "terms"
  | "privacy"
  | "security-contact";

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

const PAGE_SIZE = 10;

const pageInfo = {
  "url-statistics": {
    title: "URL 통계",
    description: "이메일에서 탐지된 URL의 위험도 통계를 확인할 수 있습니다.",
  },
  "my-url": {
    title: "나의 URL",
    description: "내 이메일에서 추출된 URL과 위험도 분석 결과를 확인합니다.",
  },
  "url-library": {
    title: "전체 URL 모음",
    description: "전체 URL 분석 결과를 모아 확인합니다.",
  },
};

function PlusIcon() {
  return <span>+</span>;
}

function MinusIcon() {
  return <span>-</span>;
}

function UrlPage({
  theme,
  currentView,
  isLoggedIn = false,
  userName = "팀코",
  onLogout,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
}: UrlPageProps) {
  const [openedUrlId, setOpenedUrlId] = useState<number | null>(null);
  const [urlItems, setUrlItems] = useState<MyUrlItem[]>([]);
  const [selectedAccount, setSelectedAccount] = useState("1234@5678.com");
  const [currentPage, setCurrentPage] = useState(1);

  const isMyUrl = currentView === "my-url";
  const isStatistics = currentView === "url-statistics";

  useEffect(() => {
    const fetchUrls = async () => {
      try {
        const response = await getMyUrls({
          page: 0,
          size: 100,
        });

        setUrlItems(response.urls);
      } catch (error) {
        console.error(error);
      }
    };

    fetchUrls();
  }, []);

  const handleChangeMenu = (view: UrlViewMode) => {
    setCurrentPage(1);
    setOpenedUrlId(null);
    onNavigate(view);
  };

 const handleToggleReason = (urlId: number) => {
  setOpenedUrlId((prevId) => (prevId === urlId ? null : urlId));
};

 
  const myTotalCount = urlItems.length;
  const myHighRiskCount = urlItems.filter(
    (item) => item.riskLevel === "DANGER"
  ).length;

  const chartData = [
    {
      name: "안전",
      value: urlItems.filter((item) => item.riskLevel === "SAFE").length,
    },
    {
      name: "주의",
      value: urlItems.filter((item) => item.riskLevel === "WARNING").length,
    },
    {
      name: "위험",
      value: urlItems.filter((item) => item.riskLevel === "DANGER").length,
    },
  ];

  const totalPages = Math.ceil(urlItems.length / PAGE_SIZE);
  const pagedUrlItems = urlItems.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE
  );

  return (
    <div className="dashboard-shell">
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

      <main className="page-main">
        <div className="page-layout">
          <aside className="page-sidebar">
            <button className="back-button" onClick={onGoHome} type="button">
              뒤로가기
            </button>

            <div className="page-side-card">
              <div className="page-side-title">URL 관리</div>

              <button
                className={
                  currentView === "url-statistics"
                    ? "side-menu-button is-active"
                    : "side-menu-button"
                }
                onClick={() => handleChangeMenu("url-statistics")}
                type="button"
              >
                URL 통계
              </button>

              <button
                className={
                  currentView === "my-url"
                    ? "side-menu-button is-active"
                    : "side-menu-button"
                }
                onClick={() => handleChangeMenu("my-url")}
                type="button"
              >
                나의 URL
              </button>

              <button
                className={
                  currentView === "url-library"
                    ? "side-menu-button is-active"
                    : "side-menu-button"
                }
                onClick={() => handleChangeMenu("url-library")}
                type="button"
              >
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
                    <strong>{myTotalCount.toLocaleString()}개</strong>
                    <p>연동된 이메일에서 탐지된 전체 링크 수입니다.</p>
                  </div>

                  <div className="url-stat-card">
                    <span>고위험 링크</span>
                    <strong>{myHighRiskCount.toLocaleString()}개</strong>
                    <p>DANGER로 분류된 링크 수입니다.</p>
                  </div>

                  <div className="url-stat-chart">
                    <ChartBox
                      title="내 URL 위험도 통계"
                      caption="이메일 링크 분석 기준"
                      total={`${myTotalCount.toLocaleString()}개`}
                      data={chartData}
                    />
                  </div>
                </section>
              ) : (
                <>
                  {isMyUrl && (
                    <div className="url-top-bar">
                      <select
                        className="url-filter-select"
                        value={selectedAccount}
                        onChange={(event) =>
                          setSelectedAccount(event.target.value)
                        }
                      >
                        <option value="1234@5678.com">1234@5678.com</option>
                        <option value="8765@4321.com">8765@4321.com</option>
                        <option value="abcd@efgh.com">abcd@efgh.com</option>
                      </select>
                    </div>
                  )}

                  <section className="url-list-card">
                    <div className="url-list-head">
                      <div>
                        <h2 className="url-list-title">
                          {pageInfo[currentView].title}
                        </h2>
                        <p className="url-list-count">
                          총 <strong>{urlItems.length}</strong>건 · 최신 링크가
                          위에 표시됩니다.
                        </p>
                      </div>
                    </div>

                    <div
                      className={
                        isMyUrl
                          ? "url-table-grid url-table-header-row"
                          : "url-table-grid url-library-table-grid url-table-header-row"
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
                      {pagedUrlItems.map((item, index) => {
                        const isOpened = openedUrlId === item.urlId;
                        const displayNumber =
                          urlItems.length -
                          ((currentPage - 1) * PAGE_SIZE + index);

                        return (
                          <div className="url-row-block" key={item.urlId}>
                            <div
                              className={
                                isMyUrl
                                  ? "url-table-grid url-table-data-row"
                                  : "url-table-grid url-library-table-grid url-table-data-row"
                              }
                            >
                              <span className="url-number-text">
                                {displayNumber}
                              </span>

                              {isMyUrl && (
                                <span>
                                  <strong>
                                    {item.senderName ?? item.domain ?? "알 수 없음"}
                                  </strong>
                                </span>
                              )}

                              <span className="url-link-text">
                                {item.normalizedUrl}
                              </span>

                              <span>
                                {item.createdAt?.slice(0, 10)}{" "}
                                {item.createdAt?.slice(11, 16)}
                              </span>

                              <span>
                                <span
                                  className={`risk-badge ${getRiskClassName(
                                    item.riskLevel as RiskLevelLabel
                                  )}`}
                                >
                                  {getRiskLabel(
                                    item.riskLevel as RiskLevelLabel
                                  )}
                                </span>
                              </span>

                              <button
                                className="url-detail-toggle"
                                onClick={() => handleToggleReason(item.urlId)}
                                type="button"
                                aria-label={
                                  isOpened ? "URL 설명 닫기" : "URL 설명 열기"
                                }
                              >
                                {isOpened ? <MinusIcon /> : <PlusIcon />}
                              </button>
                            </div>


                            {isOpened && (
  <div className="url-risk-reason-box">
    <strong>LLM 분석 결과</strong>
    <p>· 위험도: {item.riskLevel}</p>
    <p>· 점수: {item.score ?? "-"}</p>
    <p>· 설명: {item.reasonSummary ?? "분석 설명이 없습니다."}</p>

    {item.detectedRules?.map((rule) => (
      <p key={rule}>· {rule}</p>
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
                          <button
                            key={index + 1}
                            className={
                              currentPage === index + 1
                                ? "url-page-button is-active"
                                : "url-page-button"
                            }
                            onClick={() => setCurrentPage(index + 1)}
                            type="button"
                          >
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
        <button type="button" onClick={() => onNavigate("service-info")}>
          서비스 소개
        </button>
        <button type="button" onClick={() => onNavigate("terms")}>
          이용약관
        </button>
        <button type="button" onClick={() => onNavigate("privacy")}>
          개인정보 처리방침
        </button>
        <button type="button" onClick={() => onNavigate("security-contact")}>
          보안 문의
        </button>
      </footer>
    </div>
  );
}

export default UrlPage;