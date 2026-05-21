import { useState } from "react";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import "../styles/Dashboard.css";

type ThemeMode = "light" | "dark";

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

interface SecurityContactPageProps {
  theme: ThemeMode;
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

function SecurityContactPage({
  theme,
  isLoggedIn = false,
  userName = "팀코",
  onLogout,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
}: SecurityContactPageProps) {
  const [inquiryContent, setInquiryContent] = useState("");

  return (
    <div className="dashboard-shell">
      <Header
        currentView="security-contact"
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
              <div className="page-side-title">보안 문의</div>

              <button className="side-menu-button is-active" type="button">
                보안 문의
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            <div className="security-section">
              <div className="page-head">
                <h1>보안 문의</h1>
              </div>

              <div className="security-connect-box">
                <button
                  className="primary-button"
                  type="button"
                  onClick={() => alert("문의가 접수되었습니다.")}
                >
                  문의 접수
                </button>
              </div>

              <div className="security-connect-divider" />

              <div className="security-connected-section">
                <h2>문의 내용</h2>

                <textarea
                  className="security-connect-input"
                  style={{ minHeight: "160px", resize: "vertical" }}
                  placeholder="문의 내용을 입력해주세요."
                  value={inquiryContent}
                  onChange={(event) => setInquiryContent(event.target.value)}
                />

                <div className="security-connected-list">
                  <div className="security-connected-row">
                    <span>보안 취약점 제보</span>
                  </div>
                  <div className="security-connected-row">
                    <span>의심스러운 동작 문의</span>
                  </div>
                  <div className="security-connected-row">
                    <span>기타 보안 관련 문의</span>
                  </div>
                </div>
              </div>
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

export default SecurityContactPage;
