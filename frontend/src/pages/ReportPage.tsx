import { useState } from "react";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import "../styles/ReportPage.css";

type ThemeMode = "light" | "dark";
type ReportViewMode = "report-guide" | "report";

type PageViewTarget =
  | "guide"
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

interface ReportPageProps {
  theme: ThemeMode;
  currentView: ReportViewMode;
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

function ReportPage({
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
}: ReportPageProps) {
  const [reportUrl, setReportUrl] = useState("");
  const [reportReason, setReportReason] = useState("");

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
      <Navbar onNavigate={(view) => onNavigate(view as PageViewTarget)} />

      <main className="page-main">
        <div className="page-layout">
          <aside className="page-sidebar">
            <button className="back-button" onClick={onGoHome} type="button">
              뒤로가기
            </button>

            <div className="page-side-card">
              <div className="page-side-title">신고하기</div>

              <button
                className={
                  currentView === "report-guide"
                    ? "side-menu-button is-active"
                    : "side-menu-button"
                }
                onClick={() => onNavigate("report-guide")}
                type="button"
              >
                신고 안내
              </button>

              <button
                className={
                  currentView === "report"
                    ? "side-menu-button is-active"
                    : "side-menu-button"
                }
                onClick={() => onNavigate("report")}
                type="button"
              >
                신고하기
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            {currentView === "report-guide" ? (
              <div className="report-section">
                <div className="page-head">
                  <p className="eyebrow"></p>
                  <h1>신고 안내</h1>
                </div>

                <p className="report-description">
                  ※ 기관명 클릭 시 해당 신고 사이트로 이동합니다.
                </p>

                <div className="report-content-grid">
                  <section className="report-list-card">
                    <div className="report-table report-table-header">
                      <span>유형</span>
                      <span>기관</span>
                      <span>설명</span>
                    </div>

                    <div className="report-table-body">
                      <div className="report-table-row">
                        <span>
                          <strong>🔐 개인정보 유출 및 침해</strong>
                        </span>
                        <span>
                          <a
                            href="https://privacy.kisa.or.kr/"
                            className="report-link"
                          >
                            KISA 개인정보침해 신고센터
                          </a>
                        </span>
                        <span>개인정보 유출, 계정 도용, 사칭 사이트 신고</span>
                      </div>

                      <div className="report-table-row">
                        <span>
                          <strong>🛡️ 해킹·랜섬웨어·DDoS</strong>
                        </span>
                        <span>
                          <a
                            href="https://www.krcert.or.kr/kr/subPage.do?menuNo=205033"
                            className="report-link"
                          >
                            KISA 보호나라
                          </a>
                        </span>
                        <span>해킹, 악성코드, 랜섬웨어 등 침해사고 신고</span>
                      </div>

                      <div className="report-table-row">
                        <span>
                          <strong>📩 불법 스팸 문자·메일</strong>
                        </span>
                        <span>
                          <a
                            href="https://spam.kisa.or.kr/spam/main.do"
                            className="report-link"
                          >
                            KISA 불법스팸대응센터
                          </a>
                        </span>
                        <span>스팸 문자, 피싱 메일, 광고성 메시지 신고</span>
                      </div>
                    </div>
                  </section>

                  <section className="report-detail-card">
                    <div className="report-detail-head">
                      <h2>상담 센터</h2>
                    </div>

                    <div className="report-meta">
                      <p>
                        <strong>☎️ KISA 상담센터</strong> (국번없이 118)
                      </p>
                      <p>
                        <a
                          href="https://www.kisa.or.kr/118"
                          className="report-link"
                        >
                          {" "}
                          https://www.kisa.or.kr/118{" "}
                        </a>
                      </p>
                    </div>

                    <div className="report-divider" />
                  </section>
                </div>
              </div>
            ) : (
              <div className="report-section">
                <div className="page-head">
                  <p className="eyebrow"></p>
                  <h1>신고하기</h1>
                </div>

                <div className="report-connect-box">
                  <input
                    className="report-connect-input"
                    placeholder="신고할 URL을 입력하세요."
                    type="text"
                    value={reportUrl}
                    onChange={(event) => setReportUrl(event.target.value)}
                  />

                  <button
                    className="primary-button"
                    type="button"
                    onClick={() => alert("신고가 접수되었습니다.")}
                  >
                    신고 접수
                  </button>
                </div>

                <div className="report-connect-divider" />

                <div className="connected-report-section">
                  <h2>신고 사유</h2>

                  <textarea
                    className="report-connect-input"
                    style={{ minHeight: "160px", resize: "vertical" }}
                    placeholder="의심되는 이유를 입력해주세요."
                    value={reportReason}
                    onChange={(event) => setReportReason(event.target.value)}
                  />

                  <div className="connected-report-list">
                    <div className="connected-report-row">
                      <span>피싱 사이트로 의심됩니다.</span>
                    </div>
                    <div className="connected-report-row">
                      <span>로그인 정보를 요구합니다.</span>
                    </div>
                    <div className="connected-report-row">
                      <span>알 수 없는 파일 다운로드를 유도합니다.</span>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </section>
        </div>
      </main>

      <footer className="footer">
        <button type="button" onClick={() => onNavigate("guide")}>
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

export default ReportPage;
