import { useState } from "react";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import "../styles/ClassificationPage.css";
import {
  getRiskClassName,
  getRiskLabel,
  type RiskLevelLabel,
} from "../utils/riskLevel";

type ThemeMode = "light" | "dark";
type ClassificationViewMode =
  | "classification-method"
  | "classification-criteria";

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

interface ClassificationItem {
  id: number;
  title: string;
  description: string;
  detail: string;
  status: RiskLevelLabel;
}

interface ClassificationPageProps {
  theme: ThemeMode;
  currentView: ClassificationViewMode;
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

const classificationCriteria: ClassificationItem[] = [
  {
    id: 1,
    title: "안전",
    description: "정상 서비스 URL",
    detail: "공식 서비스 도메인 또는 신뢰 가능한 URL입니다.",
    status: "안전",
  },
  {
    id: 2,
    title: "의심",
    description: "의심 요소 존재",
    detail: "출처나 패턴이 명확하지 않아 추가 확인이 필요한 URL입니다.",
    status: "의심",
  },
  {
    id: 3,
    title: "주의",
    description: "추가 확인 필요",
    detail: "신규 도메인 또는 단축 URL 등 추가 분석이 필요합니다.",
    status: "주의",
  },
  {
    id: 4,
    title: "위험",
    description: "피싱 가능성 높음",
    detail: "로그인 유도 및 개인정보 입력을 요구하는 URL입니다.",
    status: "위험",
  },
  {
    id: 5,
    title: "심각",
    description: "즉시 차단 대상",
    detail: "악성코드 배포 또는 계정 탈취 시도가 확인되었습니다.",
    status: "심각",
  },
];

function ClassificationPage({
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
}: ClassificationPageProps) {
  const items = classificationCriteria;
  const [selectedItem, setSelectedItem] = useState<ClassificationItem | null>(
    null,
  );

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
              <div className="page-side-title">분류기준</div>
              <button
                className="side-menu-button is-active"
                type="button"
                onClick={() => onNavigate("classification-criteria")}
              >
                분류 기준
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            <div className="classification-section">
              <div className="page-head">
                <p className="eyebrow"></p>

                <h1>분류 기준</h1>
              </div>

              <p className="report-description">
                URL GUARD는 수집된 URL 데이터를 기반으로 위험도를 분석하고 내부
                기준에 따라 자동 분류합니다.
              </p>

              <div className="classification-content-grid">
                <section className="classification-list-card">
                  <div className="classification-table-header">
                    <span>항목</span>
                    <span>설명</span>
                    <span>상세 내용</span>
                  </div>

                  <div className="classification-table-body">
                    {items.map((item) => (
                      <button
                        key={item.id}
                        className="classification-table-row"
                        onClick={() => setSelectedItem(item)}
                        type="button"
                      >
                        <span className="classification-status-cell">
                          <strong>{item.title}</strong>
                          <span
                            className={`risk-badge ${getRiskClassName(item.status)}`}
                          >
                            {getRiskLabel(item.status)}
                          </span>
                        </span>

                        <span>{item.description}</span>

                        <span>{item.detail}</span>
                      </button>
                    ))}
                  </div>
                </section>
                {selectedItem && (
                  <section className="classification-detail-card">
                    <div className="classification-detail-box">
                      <div className="classification-detail-head">
                        <h2>{selectedItem.title}</h2>
                        <span
                          className={`risk-badge ${getRiskClassName(selectedItem.status)}`}
                        >
                          {getRiskLabel(selectedItem.status)}
                        </span>
                      </div>

                      <div className="classification-meta">
                        <p>
                          <strong>분류 설명 :</strong>{" "}
                          {selectedItem.description}
                        </p>

                        <p>
                          <strong>상세 기준 :</strong> {selectedItem.detail}
                        </p>
                      </div>
                    </div>
                  </section>
                )}
              </div>
            </div>
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

export default ClassificationPage;
