import { useEffect, useState } from "react";
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
  analyzeUrlWithLlm,
  getUrls,
  type UrlListItem,
  type MyUrlItem,
  type LlmAnalysisResponse,
} from "../api/urlApi";

type ThemeMode = "light" | "dark";
type UrlViewMode = "my-url" | "url-library";

type PageViewTarget =
  | "my-mailbox"
  | "mail-connect"
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

const pageInfo = {
  "my-url": {
    title: "나의 URL",
    description: "연동된 이메일에서 탐지된 URL 분석 결과를 확인할 수 있습니다.",
  },
  "url-library": {
    title: "전체 URL 모음",
    description: "수집된 URL 분석 결과를 목록 형태로 확인할 수 있습니다.",
  },
};

function PlusIcon() {
  return <span>+</span>;
}

function MinusIcon() {
  return <span>-</span>;
}

function parseLlmReasonSummary(reasonSummary?: string | null) {
  if (!reasonSummary) {
    return {
      reason: "분석 설명이 아직 없습니다.",
      recommendation: "의심스러운 링크는 클릭하지 않는 것이 좋습니다.",
      confidence: null as number | null,
      falsePositivePossibility: null as boolean | null,
    };
  }

  try {
    const parsed = JSON.parse(reasonSummary);

    return {
      reason: parsed.reason ?? reasonSummary,
      recommendation:
        parsed.recommendation ?? "의심스러운 링크는 클릭하지 않는 것이 좋습니다.",
      confidence:
        typeof parsed.confidence === "number" ? parsed.confidence : null,
      falsePositivePossibility:
        typeof parsed.falsePositivePossibility === "boolean"
          ? parsed.falsePositivePossibility
          : null,
    };
  } catch {
    return {
      reason: reasonSummary,
      recommendation: "의심스러운 링크는 클릭하지 않는 것이 좋습니다.",
      confidence: null as number | null,
      falsePositivePossibility: null as boolean | null,
    };
  }
}

function UrlPage({
  theme,
  currentView,
  isLoggedIn = false,
  userName = "사용자",
  onLogout,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
}: UrlPageProps) {
  const [openedUrlId, setOpenedUrlId] = useState<number | null>(null);
  const [urlItems, setUrlItems] = useState<(MyUrlItem | UrlListItem)[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [llmResult, setLlmResult] = useState<LlmAnalysisResponse | null>(null);
  const [analyzingUrlId, setAnalyzingUrlId] = useState<number | null>(null);
  const [selectedLlmUrlId, setSelectedLlmUrlId] = useState<number | null>(null);

  const isMyUrl = currentView === "my-url";

  useEffect(() => {
  const fetchUrls = async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");
      setUrlItems([]);
      setOpenedUrlId(null);
      setLlmResult(null);
      setSelectedLlmUrlId(null);

      if (currentView === "my-url") {
        const response = await getMyUrls({
          page: 0,
          size: 20,
        });

        setUrlItems(response.urls ?? []);
      } else {
        const response = await getUrls({
          page: 0,
          size: 20,
        });

        setUrlItems(response.urls ?? []);
      }
    } catch (error) {
      console.error(error);
      setErrorMessage(
        "URL 목록을 불러오지 못했습니다. 로그인 또는 서버 상태를 확인해주세요.",
      );
    } finally {
      setIsLoading(false);
    }
  };

  fetchUrls();
}, [currentView]);

  const handleChangeMenu = (nextView: UrlViewMode) => {
    onNavigate(nextView);
  };

  const handleToggleReason = (id: number) => {
    setOpenedUrlId((prevId) => (prevId === id ? null : id));
  };

  const handleLlmAnalyze = async (urlId: number) => {
    try {
      setAnalyzingUrlId(urlId);

      const result = await analyzeUrlWithLlm(urlId);

      setLlmResult(result);
      setSelectedLlmUrlId(urlId);

      alert("LLM 분석이 완료되었습니다.");
    } catch (error) {
      console.error(error);
      alert("LLM 분석 중 오류가 발생했습니다.");
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

              <section className="url-list-card">
                <div className="url-list-head">
                  <div>
                    <h2 className="url-list-title">
                      {pageInfo[currentView].title}
                    </h2>
                    <p className="url-list-count">
                      총 <strong>{urlItems.length}</strong>건 · 최신 링크가 위에
                      표시됩니다.
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
                    {isLoading && (
                  <div className="url-empty-message">
                    URL 목록을 불러오는 중입니다...
                  </div>
                )}

                {!isLoading && errorMessage && (
                  <div className="url-empty-message">
                    {errorMessage}
                  </div>
                )}

                {!isLoading && !errorMessage && urlItems.length === 0 && (
                  <div className="url-empty-message">
                    표시할 URL이 없습니다.
                  </div>
                  )}

  {!isLoading &&
    !errorMessage &&
    urlItems.map((item, index) => {
                    const isOpened = openedUrlId === item.urlId;
                    const displayNumber = urlItems.length - index;
                    const riskLevel = item.riskLevel as RiskLevelLabel;

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
                                {"senderName" in item && item.senderName
                                  ? item.senderName
                                  : "알 수 없음"}
                              </strong>
                            </span>
                          )}

                          <span className="url-link-text">
                            {item.normalizedUrl}
                          </span>

                          <span>
                            {item.createdAt
                              ? new Date(item.createdAt).toLocaleString()
                              : "-"}
                          </span>

                          <span>
                            <span
                              className={`risk-badge ${getRiskClassName(
                                riskLevel,
                              )}`}
                            >
                              {getRiskLabel(riskLevel)}
                            </span>
                          </span>

                          <div className="url-action-buttons">
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

                            <button
                              className="url-detail-toggle"
                              onClick={() => handleLlmAnalyze(item.urlId)}
                              type="button"
                              disabled={analyzingUrlId === item.urlId}
                            >
                              {analyzingUrlId === item.urlId
                                ? "분석 중..."
                                : "LLM 분석"}
                            </button>
                          </div>
                        </div>

                        {isOpened && (
                          <div className="url-risk-reason-box">
                            <strong>위험 분석 결과</strong>

                            <p>· 위험도: {getRiskLabel(riskLevel)}</p>

                            <p>
                              · 점수:{" "}
                              {item.score !== null && item.score !== undefined
                                ? item.score
                                : "-"}
                            </p>

                            <p>
                              · 설명:{" "}
                              {item.reasonSummary
                                ? item.reasonSummary
                                : "분석 설명이 아직 없습니다."}
                            </p>

                            {item.detectedRules?.map((rule) => (
                              <p key={rule}>· 탐지 규칙: {rule}</p>
                            ))}
                          </div>
                        )}

                        {llmResult && selectedLlmUrlId === item.urlId && (() => {
                          const parsedSummary = parseLlmReasonSummary(llmResult.reasonSummary);

                            return (
                              <div className="url-risk-reason-box">
                              <strong>LLM 분석 결과</strong>

                              <p>· 위험도: {llmResult.risk}</p>
                              <p>· 점수: {llmResult.score}점</p>

                            {parsedSummary.confidence !== null && (
                              <p>· 신뢰도: {Math.round(parsedSummary.confidence * 100)}%</p>
                            )}

                            {parsedSummary.falsePositivePossibility !== null && (
                              <p>
                                · 오탐 가능성:{" "}
                            {parsedSummary.falsePositivePossibility ? "있음" : "낮음"}
                              </p>
                            )}

                              <p>· 위험 사유: {parsedSummary.reason}</p>
                              <p>· 권장 조치: {parsedSummary.recommendation}</p>

                            {llmResult.detectedRules?.map((rule) => (
                              <p key={rule}>· 탐지 규칙: {rule}</p>
                            ))}
                            </div>
                            );
                            })()}
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