import { useMemo, useState } from 'react';
import Header from '../components/Header';
import Navbar from '../components/Navbar';
import '../styles/Dashboard.css';

type ThemeMode = 'light' | 'dark';
type NotificationViewMode = 'notifications' | 'notification-settings';

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

interface NotificationItem {
  id: number;
  title: string;
  summary: string;
  date: string;
  type: '위험 탐지' | '연동 상태' | '신고 처리';
  content: string;
  isRead: boolean;
}

interface NotificationPageProps {
  theme: ThemeMode;
  currentView: NotificationViewMode;
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

const notificationSeed: NotificationItem[] = [
  {
    id: 1,
    title: '고위험 URL이 탐지되었습니다.',
    summary: '메일 기반 수집 데이터에서 매우 위험 URL 3건이 추가 탐지되었습니다.',
    date: '2026.03.25 12:34',
    type: '위험 탐지',
    content:
      '메일 기반 수집 데이터에서 매우 위험 URL 3건이 추가 탐지되었습니다. URL 관리 페이지에서 상세 링크와 위험도를 확인해 주세요.',
    isRead: false,
  },
  {
    id: 2,
    title: '이메일 연동 상태가 정상입니다.',
    summary: '연동된 메일 계정의 동기화가 정상적으로 완료되었습니다.',
    date: '2026.03.25 10:15',
    type: '연동 상태',
    content:
      '연동된 메일 계정의 동기화가 정상적으로 완료되었습니다. 마지막 점검 시간은 10:15이며 현재 오류는 없습니다.',
    isRead: true,
  },
  {
    id: 3,
    title: '신고 접수 건 처리 상태가 변경되었습니다.',
    summary: '사용자가 신고한 URL 1건이 검토 완료 상태로 변경되었습니다.',
    date: '2026.03.24 17:02',
    type: '신고 처리',
    content:
      '사용자가 신고한 URL 1건이 검토 완료 상태로 변경되었습니다. 신고 내역 페이지에서 조치 결과를 확인할 수 있습니다.',
    isRead: false,
  },
];

function NotificationPage({
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
}: NotificationPageProps) {
  const [notifications, setNotifications] = useState(notificationSeed);
  const [selectedId, setSelectedId] = useState<number>(notificationSeed[0].id);

  const [pushEnabled, setPushEnabled] = useState(true);
  const [mailEnabled, setMailEnabled] = useState(true);
  const [reportEnabled, setReportEnabled] = useState(true);
  const [systemEnabled, setSystemEnabled] = useState(false);

  const selectedNotification = useMemo(
    () => notifications.find((item) => item.id === selectedId) ?? notifications[0],
    [notifications, selectedId],
  );

  const handleSelectNotification = (id: number) => {
    setSelectedId(id);
    setNotifications((prev) =>
      prev.map((item) => (item.id === id ? { ...item, isRead: true } : item)),
    );
  };

  const handleSaveNotificationSettings = () => {
    const confirmed = window.confirm('저장하시겠습니까?');

    if (confirmed) {
      alert('저장 완료했습니다.');
    }
  };

  const unreadCount = notifications.filter((item) => !item.isRead).length;

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
              <div className="page-side-title">알림함</div>

              <button
                className={
                  currentView === 'notifications' ? 'side-menu-button is-active' : 'side-menu-button'
                }
                onClick={() => onNavigate('notifications')}
                type="button"
              >
                알림함
              </button>

              <button
                className={
                  currentView === 'notification-settings'
                    ? 'side-menu-button is-active'
                    : 'side-menu-button'
                }
                onClick={() => onNavigate('notification-settings')}
                type="button"
              >
                알림 설정
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            {currentView === 'notifications' ? (
              <div className="notification-section">
                <div className="notification-summary-card">
                  <div>
                    <p className="eyebrow"></p>
                    <h2>알림함</h2>
                  </div>
                  <div className="notification-summary-badge">
                    <span>읽지 않은 알림</span>
                    <strong>{unreadCount}건</strong>
                  </div>
                </div>

                <div className="notification-content-grid">
                  <section className="notification-list-card">
                    <div className="notification-list-head">
                      <h3>알림 목록</h3>
                    </div>

                    <div className="notification-list">
                      {notifications.map((item) => (
                        <button
                          key={item.id}
                          className={
                            selectedNotification?.id === item.id
                              ? 'notification-item is-active'
                              : 'notification-item'
                          }
                          onClick={() => handleSelectNotification(item.id)}
                          type="button"
                        >
                          <div className="notification-item-top">
                            <span
                              className={
                                item.isRead ? 'notification-read-state is-read' : 'notification-read-state'
                              }
                            >
                              {item.isRead ? '읽음' : '새 알림'}
                            </span>
                            <small>{item.date}</small>
                          </div>
                          <strong>{item.title}</strong>
                          <p>{item.summary}</p>
                        </button>
                      ))}
                    </div>
                  </section>

                  <section className="notification-detail-card">
                    <div className="notification-detail-head">
                      <div>
                        <p className="eyebrow"></p>
                        <h2>{selectedNotification.title}</h2>
                      </div>
                      <span className="notification-type-pill">{selectedNotification.type}</span>
                    </div>

                    <div className="notification-detail-meta">
                      <span>{selectedNotification.date}</span>
                    </div>

                    <div className="mail-divider" />

                    <div className="notification-detail-body">
                      <p>{selectedNotification.content}</p>
                    </div>
                  </section>
                </div>
              </div>
            ) : (
              <div className="notification-section">
                <div className="mypage-head">
                  <p className="eyebrow"></p>
                  <h1>알림 설정</h1>
                </div>

                <div className="notification-settings-card">
                  <div className="notification-setting-row">
                    <div>
                      <strong>위험 URL 탐지 알림</strong>
                      <p>고위험 URL이 새로 탐지되면 알림을 받습니다.</p>
                    </div>
                    <label className="toggle-switch">
                      <input
                        type="checkbox"
                        checked={pushEnabled}
                        onChange={(event) => setPushEnabled(event.target.checked)}
                      />
                      <span className="toggle-slider" />
                    </label>
                  </div>

                  <div className="notification-setting-row">
                    <div>
                      <strong>이메일 연동 상태 알림</strong>
                      <p>메일 연동 상태 변화나 동기화 오류 발생 시 알림을 받습니다.</p>
                    </div>
                    <label className="toggle-switch">
                      <input
                        type="checkbox"
                        checked={mailEnabled}
                        onChange={(event) => setMailEnabled(event.target.checked)}
                      />
                      <span className="toggle-slider" />
                    </label>
                  </div>

                  <div className="notification-setting-row">
                    <div>
                      <strong>신고 처리 결과 알림</strong>
                      <p>신고 접수 후 처리 상태가 변경되면 알림을 받습니다.</p>
                    </div>
                    <label className="toggle-switch">
                      <input
                        type="checkbox"
                        checked={reportEnabled}
                        onChange={(event) => setReportEnabled(event.target.checked)}
                      />
                      <span className="toggle-slider" />
                    </label>
                  </div>

                  <div className="notification-setting-row">
                    <div>
                      <strong>시스템 점검 안내 알림</strong>
                      <p>서비스 점검, 공지, 정책 변경 등의 알림을 받습니다.</p>
                    </div>
                    <label className="toggle-switch">
                      <input
                        type="checkbox"
                        checked={systemEnabled}
                        onChange={(event) => setSystemEnabled(event.target.checked)}
                      />
                      <span className="toggle-slider" />
                    </label>
                  </div>
                </div>

                <div className="notification-actions">
                  <button
                    className="primary-button"
                    type="button"
                    onClick={handleSaveNotificationSettings}
                  >
                    저장하기
                  </button>
                  <button className="secondary-button" type="button">
                    취소
                  </button>
                </div>
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

export default NotificationPage;