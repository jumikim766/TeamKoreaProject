import { useState } from "react";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import "../styles/ReportPage.css";

type ThemeMode = "light" | "dark";
type ReportViewMode = "report-guide" | "report";

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
      <Navbar onNavigate={onNavigate} />

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

                <div className="report-content-grid">
                  <section className="report-list-card">
                    <div className="report-table report-table-header">
                      <span>단계</span>
                      <span>내용</span>
                      <span>설명</span>
                      <span>상태</span>
                    </div>

                    <div className="report-table-body">
                      <button
                        className="report-table-row is-active"
                        type="button"
                      >
                        <span>
                          <strong>1단계</strong>
                          <small>URL 확인</small>
                        </span>
                        <span>의심 URL 복사</span>
                        <span>
                          메일, 문자, 웹사이트에서 의심되는 URL을 확인합니다.
                        </span>
                        <span>필수</span>
                      </button>

                      <button className="report-table-row" type="button">
                        <span>
                          <strong>2단계</strong>
                          <small>내용 작성</small>
                        </span>
                        <span>신고 정보 입력</span>
                        <span>URL, 발견 위치, 의심 사유를 입력합니다.</span>
                        <span>필수</span>
                      </button>

                      <button className="report-table-row" type="button">
                        <span>
                          <strong>3단계</strong>
                          <small>검토 반영</small>
                        </span>
                        <span>위험도 분류</span>
                        <span>
                          신고된 URL은 검토 후 분류 기준에 반영됩니다.
                        </span>
                        <span>진행</span>
                      </button>
                    </div>
                  </section>

                  <section className="report-detail-card">
                    <div className="report-detail-head">
                      <h2>신고 접수 안내</h2>
                    </div>

                    <div className="report-meta">
                      <p>
                        <strong>신고 대상 :</strong> 피싱, 악성코드, 계정 탈취
                        의심 URL
                      </p>
                      <p>
                        <strong>필수 정보 :</strong> URL 주소, 발견 위치, 신고
                        사유
                      </p>
                      <p>
                        <strong>처리 방식 :</strong> 접수 후 위험도 분석
                      </p>
                    </div>

                    <div className="report-divider" />

                    <div className="report-body">
                      <p>
                        신고된 URL은 내부 기준에 따라 검토되며, 위험도에 따라
                        안전, 주의, 위험, 매우 위험으로 분류됩니다.
                      </p>
                    </div>
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

export default ReportPage;
