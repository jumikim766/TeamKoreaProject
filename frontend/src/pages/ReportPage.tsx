import Header from '../components/Header';
import '../styles/Dashboard.css';

type ThemeMode = 'light' | 'dark';
type ReportViewMode = 'report-guide' | 'report';

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

interface ReportPageProps {
  theme: ThemeMode;
  currentView: ReportViewMode;
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
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
}: ReportPageProps) {
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
              <div className="page-side-title">신고하기</div>

              <button
                className={
                  currentView === 'report-guide' ? 'side-menu-button is-active' : 'side-menu-button'
                }
                onClick={() => onNavigate('report-guide')}
                type="button"
              >
                신고 안내
              </button>

              <button
                className={currentView === 'report' ? 'side-menu-button is-active' : 'side-menu-button'}
                onClick={() => onNavigate('report')}
                type="button"
              >
                신고하기
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            {currentView === 'report-guide' ? (
              <div className="placeholder-section">
                <div className="placeholder-head">
                  <p className="eyebrow">Report guide</p>
                  <h1>신고 안내</h1>
                </div>

                <div className="placeholder-card-grid">
                  <section className="placeholder-card">
                    <h2>신고 절차 안내</h2>
                    <p>내용 채우기</p>
                  </section>

                  <section className="placeholder-card">
                    <h2>신고 대상 기준</h2>
                    <p>내용 채우기</p>
                  </section>
                </div>

                <section className="placeholder-wide-card">
                  <h2>추가 안내 사항</h2>
                  <p>내용 채우기</p>
                </section>
              </div>
            ) : (
              <div className="placeholder-section">
                <div className="placeholder-head">
                  <p className="eyebrow">Report form</p>
                  <h1>신고하기</h1>
                </div>

                <section className="form-placeholder-card">
                  <div className="form-placeholder-row">
                    <span>신고 제목</span>
                    <div>내용 채우기</div>
                  </div>

                  <div className="form-placeholder-row">
                    <span>URL / 대상</span>
                    <div>내용 채우기</div>
                  </div>

                  <div className="form-placeholder-row">
                    <span>상세 내용</span>
                    <div className="textarea-placeholder">내용 채우기</div>
                  </div>

                  <div className="placeholder-actions">
                    <button className="primary-button" type="button">
                      제출하기
                    </button>
                    <button className="secondary-button" type="button">
                      취소
                    </button>
                  </div>
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

export default ReportPage;