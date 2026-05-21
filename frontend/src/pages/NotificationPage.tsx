


import { useEffect, useMemo, useState } from 'react';
import Header from '../components/Header';
import Navbar from '../components/Navbar';
import {
  getNotifications,
  getUnreadCount,
  readNotification,
  type NotificationResponse,
} from '../api/notificationApi';
import '../styles/NotificationPage.css';

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
  type: string;
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

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('ko-KR');
};

const convertNotification = (item: NotificationResponse): NotificationItem => ({
  id: item.notificationId,
  title: item.title,
  summary: item.message,
  content: item.message,
  date: formatDate(item.createdAt),
  type: '알림',
  isRead: item.isRead,
});

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
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);

  const [pushEnabled, setPushEnabled] = useState(true);
  const [mailEnabled, setMailEnabled] = useState(true);
  const [reportEnabled, setReportEnabled] = useState(true);
  const [systemEnabled, setSystemEnabled] = useState(false);

  const selectedNotification = useMemo(
    () => notifications.find((item) => item.id === selectedId) ?? null,
    [notifications, selectedId],
  );

  const fetchUnreadCount = async () => {
    try {
      const count = await getUnreadCount();
      setUnreadCount(count);
      window.dispatchEvent(new Event('notification-updated'));
    } catch (error: any) {
      alert(error.response?.data?.message || '읽지 않은 알림 개수를 불러오지 못했습니다.');
    }
  };

  const fetchNotifications = async () => {
    try {
      setLoading(true);

      const data = await getNotifications();
      const converted = data.map(convertNotification);

      setNotifications(converted);

      if (converted.length > 0) {
        setSelectedId((prev) => prev ?? converted[0].id);
      } else {
        setSelectedId(null);
      }
    } catch (error: any) {
      alert(error.response?.data?.message || '알림 목록을 불러오지 못했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!isLoggedIn) return;

    fetchNotifications();
    fetchUnreadCount();
  }, [isLoggedIn, currentView]);

  useEffect(() => {
    if (!isLoggedIn) return;

    const timer = window.setInterval(() => {
      fetchNotifications();
      fetchUnreadCount();
    }, 30000);

    return () => window.clearInterval(timer);
  }, [isLoggedIn]);

  const handleSelectNotification = async (id: number) => {
    try {
      setSelectedId(id);

      const target = notifications.find((item) => item.id === id);

      if (target && !target.isRead) {
        await readNotification(id);

        setNotifications((prev) =>
          prev.map((item) =>
            item.id === id ? { ...item, isRead: true } : item,
          ),
        );

        await fetchUnreadCount();
      }
    } catch (error: any) {
      alert(error.response?.data?.message || '알림 읽음 처리에 실패했습니다.');
    }
  };

  const handleSaveNotificationSettings = () => {
    const confirmed = window.confirm('저장하시겠습니까?');

    if (confirmed) {
      alert('저장 완료했습니다.');
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
              <div className="page-side-title">알림함</div>

              <button
                className={
                  currentView === 'notifications'
                    ? 'side-menu-button is-active'
                    : 'side-menu-button'
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
                      {loading ? (
                        <div className="notification-empty">
                          알림을 불러오는 중입니다.
                        </div>
                      ) : notifications.length === 0 ? (
                        <div className="notification-empty">
                          새로운 알림이 없습니다.
                        </div>
                      ) : (
                        notifications.map((item) => (
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
                                  item.isRead
                                    ? 'notification-read-state is-read'
                                    : 'notification-read-state'
                                }
                              >
                                {item.isRead ? '읽음' : '새 알림'}
                              </span>

                              <small>{item.date}</small>
                            </div>

                            <strong>{item.title}</strong>
                            <p>{item.summary}</p>
                          </button>
                        ))
                      )}
                    </div>
                  </section>

                  <section className="notification-detail-card">
                    {selectedNotification ? (
                      <>
                        <div className="notification-detail-head">
                          <div>
                            <p className="eyebrow"></p>
                            <h2>{selectedNotification.title}</h2>
                          </div>

                          <span className="notification-type-pill">
                            {selectedNotification.type}
                          </span>
                        </div>

                        <div className="notification-detail-meta">
                          <span>{selectedNotification.date}</span>
                        </div>

                        <div className="notification-divider" />

                        <div className="notification-detail-body">
                          <p>{selectedNotification.content}</p>
                        </div>
                      </>
                    ) : (
                      <div className="notification-empty">
                        선택된 알림이 없습니다.
                      </div>
                    )}
                  </section>
                </div>
              </div>
            ) : (
              <div className="notification-section">
                <div className="page-head">
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