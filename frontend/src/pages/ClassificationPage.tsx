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
    description: "특별한 위험 요소가 발견되지 않은 정상 URL",
    detail: "0~29점",
    status: "안전",
  },
  {
    id: 2,
    title: "주의",
    description: "일부 의심 요소가 탐지되어 확인이 필요한 URL",
    detail: "30~69점",
    status: "주의",
  },
  {
    id: 3,
    title: "위험",
    description: "피싱 또는 악성 사이트일 가능성이 높은 URL",
    detail: "70~100점",
    status: "위험",
  },
];

const detectionCriteriaGroups = [
  {
    title: "URL 구조 기반",
    items: [
      {
        criteria: "IP 주소 직접 사용",
        score: "+50",
        description: "도메인 대신 IP 주소 사용",
      },
      {
        criteria: "HTTPS 미사용 (http://)",
        score: "+20",
        description: "암호화되지 않은 연결 사용",
      },
      {
        criteria: "URL 길이 과다",
        score: "+20",
        description: "비정상적으로 긴 URL",
      },
      {
        criteria: "하이픈(-) 과다 사용",
        score: "+15",
        description: "브랜드 사칭 및 혼동 유도 가능성",
      },
      {
        criteria: "서브도메인 과다 사용",
        score: "+15",
        description: "정상 사이트처럼 위장 가능성",
      },
      {
        criteria: "단축 URL 사용",
        score: "+30",
        description: "실제 목적지 숨김 가능성",
      },
      {
        criteria: "의심 TLD 사용",
        score: "+25",
        description: ".xyz, .top, .club, .biz, .tk 등",
      },
    ],
  },
  {
    title: "피싱 키워드 기반",
    items: [
      {
        criteria: "login 포함",
        score: "+20",
        description: "로그인 유도 가능성",
      },
      {
        criteria: "verify 포함",
        score: "+15",
        description: "인증/검증 사칭 가능성",
      },
      {
        criteria: "password 포함",
        score: "+20",
        description: "비밀번호 입력 유도 가능성",
      },
      {
        criteria: "account 포함",
        score: "+15",
        description: "계정 탈취 가능성",
      },
      {
        criteria: "secure 포함",
        score: "+15",
        description: "안전한 사이트처럼 위장 가능성",
      },
      {
        criteria: "bank, pay, billing, confirm, update 포함",
        score: "+20",
        description: "금융/결제/인증 사칭 가능성",
      },
    ],
  },
  {
    title: "도메인 신뢰도 기반",
    items: [
      {
        criteria: "블랙리스트 도메인",
        score: "+100",
        description: "기존 악성 도메인",
      },
      {
        criteria: "화이트리스트 도메인",
        score: "0",
        description: "신뢰 가능한 도메인",
      },
      {
        criteria: "낮은 도메인 평판",
        score: "+15",
        description: "신뢰도 낮은 도메인",
      },
    ],
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
  const [isDetectionOpen, setIsDetectionOpen] = useState(false);
  const items = classificationCriteria;

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
                    <span>점수 기준</span>
                  </div>

                  <div className="classification-table-body">
                    {items.map((item) => (
                      <div key={item.id} className="classification-table-row">
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
                      </div>
                    ))}
                  </div>
                </section>
              </div>

              <div className="classification-divider" />

              <section className="detection-criteria-section">
                <div className="classification-sub-head detection-toggle-head">
                  <h1>탐지 기준표</h1>

                  <button
                    className="detection-toggle-button"
                    type="button"
                    onClick={() => setIsDetectionOpen((prev) => !prev)}
                    aria-label={
                      isDetectionOpen
                        ? "탐지 기준표 접기"
                        : "탐지 기준표 펼치기"
                    }
                  >
                    {isDetectionOpen ? "-" : "+"}
                  </button>
                </div>

                {isDetectionOpen && (
                  <div className="detection-group-list">
                    {detectionCriteriaGroups.map((group) => (
                      <section className="detection-group" key={group.title}>
                        <h3 className="detection-group-title">
                          • {group.title}
                        </h3>

                        <div className="detection-group-card">
                          <div className="detection-table-header">
                            <span>탐지 기준</span>
                            <span>점수</span>
                            <span>설명</span>
                          </div>

                          <div className="detection-table-body">
                            {group.items.map((item) => (
                              <div
                                className="detection-table-row"
                                key={item.criteria}
                              >
                                <strong>{item.criteria}</strong>
                                <span>{item.score}</span>
                                <span>{item.description}</span>
                              </div>
                            ))}
                          </div>
                        </div>
                      </section>
                    ))}
                  </div>
                )}
              </section>
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
