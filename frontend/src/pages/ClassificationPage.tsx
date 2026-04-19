import Header from '../components/Header';
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

interface ClassificationPageProps {
  theme: ThemeMode;
  currentView: ClassificationViewMode;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
  onNavigate: (view: PageViewTarget) => void;
}

function ClassificationPage({
  theme,
  currentView,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
}: ClassificationPageProps) {
  return (
    <div className="dashboard-shell">
      <Header
        currentView={currentView}
        theme={theme}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onGoMyPage={onGoMyPage}
        onToggleTheme={onToggleTheme}
      />

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
            {currentView === 'classification-method' ? (
              <div className="placeholder-section">
                <div className="placeholder-head">
                  <p className="eyebrow">Classification method</p>
                  <h1>분류 방법</h1>
                </div>

                <div className="placeholder-card-grid">
                  <section className="placeholder-card">
                    <h2>분류 프로세스</h2>
                    <p>내용 채우기</p>
                  </section>

                  <section className="placeholder-card">
                    <h2>탐지 흐름</h2>
                    <p>내용 채우기</p>
                  </section>
                </div>

                <section className="placeholder-wide-card">
                  <h2>세부 설명</h2>
                  <p>내용 채우기</p>
                </section>
              </div>
            ) : (
              <div className="placeholder-section">
                <div className="placeholder-head">
                  <p className="eyebrow">Classification criteria</p>
                  <h1>분류 기준</h1>
                </div>

                <div className="placeholder-card-grid">
                  <section className="placeholder-card">
                    <h2>위험도 기준</h2>
                    <p>내용 채우기</p>
                  </section>

                  <section className="placeholder-card">
                    <h2>판단 규칙</h2>
                    <p>내용 채우기</p>
                  </section>
                </div>

                <section className="placeholder-wide-card">
                  <h2>추가 기준 설명</h2>
                  <p>내용 채우기</p>
                </section>
              </div>
            )}
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

export default ClassificationPage;