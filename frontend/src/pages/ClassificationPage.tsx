import Header from '../components/Header';
import Navbar from '../components/Navbar';
import '../styles/Dashboard.css';

type ThemeMode = 'light' | 'dark';
type ClassificationViewMode = 'classification-method' | 'classification-criteria';

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

interface ClassificationItem {
  id: number;
  title: string;
  description: string;
  detail: string;
  status: '매우 위험' | '위험' | '주의' | '안전';
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

const classificationMethods: ClassificationItem[] = [
  {
    id: 1,
    title: 'URL 수집',
    description: '메일 및 신고 데이터를 기반으로 URL 수집',
    detail:
      '메일함, 신고 데이터, 사용자 입력 데이터를 기반으로 URL을 수집하고 분석 대상으로 등록합니다.',
    status: '안전',
  },
  {
    id: 2,
    title: '위험 요소 분석',
    description: '도메인 및 링크 패턴 분석',
    detail:
      '도메인 생성일, URL 패턴, 로그인 유도 여부 등을 분석하여 위험 요소를 확인합니다.',
    status: '주의',
  },
  {
    id: 3,
    title: '위험도 분류',
    description: '위험도 기준에 따라 단계 분류',
    detail:
      '분석 결과를 기반으로 안전, 주의, 위험, 매우 위험 단계로 분류합니다.',
    status: '위험',
  },
];

const classificationCriteria: ClassificationItem[] = [
  {
    id: 1,
    title: '안전',
    description: '정상 서비스 URL',
    detail: '공식 서비스 도메인 또는 신뢰 가능한 URL입니다.',
    status: '안전',
  },
  {
    id: 2,
    title: '주의',
    description: '추가 확인 필요',
    detail: '신규 도메인 또는 단축 URL 등 추가 분석이 필요합니다.',
    status: '주의',
  },
  {
    id: 3,
    title: '위험',
    description: '피싱 가능성 높음',
    detail: '로그인 유도 및 개인정보 입력을 요구하는 URL입니다.',
    status: '위험',
  },
  {
    id: 4,
    title: '매우 위험',
    description: '즉시 차단 대상',
    detail: '악성코드 배포 또는 계정 탈취 시도가 확인되었습니다.',
    status: '매우 위험',
  },
];

function ClassificationPage({
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
}: ClassificationPageProps) {
  const items =
    currentView === 'classification-method'
      ? classificationMethods
      : classificationCriteria;

  const selectedItem = items[0];

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
              <div className="page-side-title">분류기준</div>

              <button
                className={
                  currentView === 'classification-method'
                    ? 'side-menu-button is-active'
                    : 'side-menu-button'
                }
                onClick={() => onNavigate('classification-method')}
                type="button"
              >
                분류 방법
              </button>

              <button
                className={
                  currentView === 'classification-criteria'
                    ? 'side-menu-button is-active'
                    : 'side-menu-button'
                }
                onClick={() => onNavigate('classification-criteria')}
                type="button"
              >
                분류 기준
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            <div className="mail-section">
              <div className="mypage-head">
                <p className="eyebrow"></p>

                <h1>
                  {currentView === 'classification-method'
                    ? '분류 방법'
                    : '분류 기준'}
                </h1>
              </div>

              <div className="mail-content-grid">
                <section className="mail-list-card">
                  <div className="mail-table mail-table-header">
                    <span>항목</span>
                    <span>설명</span>
                    <span>상세 내용</span>
                    <span>상태</span>
                  </div>

                  <div className="mail-table-body">
                    {items.map((item) => (
                      <button
                        key={item.id}
                        className="mail-table-row"
                        type="button"
                      >
                        <span>
                          <strong>{item.title}</strong>
                          <small>{item.description}</small>
                        </span>

                        <span>{item.description}</span>

                        <span>{item.detail}</span>

                        <span className={`risk-badge risk-${item.status}`}>
                          {item.status}
                        </span>
                      </button>
                    ))}
                  </div>
                </section>

                <section className="mail-detail-card">
                  <div className="mail-detail-head">
                    <h2>{selectedItem.title}</h2>

                    <span
                      className={`risk-badge risk-${selectedItem.status}`}
                    >
                      {selectedItem.status}
                    </span>
                  </div>

                  <div className="mail-meta">
                    <p>
                      <strong>분류 설명 :</strong>{' '}
                      {selectedItem.description}
                    </p>

                    <p>
                      <strong>상세 기준 :</strong>{' '}
                      {selectedItem.detail}
                    </p>

                    <p>
                      <strong>적용 상태 :</strong>{' '}
                      {selectedItem.status}
                    </p>
                  </div>

                  <div className="mail-divider" />

                  <div className="mail-body">
                    <p>
                      URL GUARD는 수집된 URL 데이터를 기반으로
                      위험도를 분석하고 내부 기준에 따라 자동 분류합니다.
                    </p>
                  </div>
                </section>
              </div>
            </div>
          </section>
        </div>
      </main>

      <footer className="footer">
        <button
          type="button"
          onClick={() => onNavigate('service-info')}
        >
          서비스 소개
        </button>

        <button
          type="button"
          onClick={() => onNavigate('terms')}
        >
          이용약관
        </button>

        <button
          type="button"
          onClick={() => onNavigate('privacy')}
        >
          개인정보 처리방침
        </button>

        <button
          type="button"
          onClick={() => onNavigate('security-contact')}
        >
          보안 문의
        </button>
      </footer>
    </div>
  );
}

export default ClassificationPage;