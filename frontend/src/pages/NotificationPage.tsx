import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import type { ViewMode } from '../App';
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

interface NotificationItem {
  id: number;
  title: string;
  summary: string;
  date: string;
  type: string;
  content: string;
  isRead: boolean;
}

const toNotificationItem = (
  notification: NotificationResponse
): NotificationItem => {
  return {
    id: notification.notificationId,
    title: notification.title,
    summary: notification.message,
    date: new Date(notification.createdAt).toLocaleString(),
    type: 'URL 알림',
    content: notification.message,
    isRead: notification.isRead,
  };
};

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
  onNavigate: (view: ViewMode) => void;
}

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
  const hasShownErrorRef = useRef(false);

  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [unreadCount, setUnreadCount] = useState(0);
  const [pushEnabled, setPushEnabled] = useState(true);

  const selectedNotification = useMemo(() => {
    return notifications.find((item) => item.id === selectedId) ?? null;
  }, [notifications, selectedId]);

  const showErrorOnce = useCallback((message: string) => {
    if (!hasShownErrorRef.current) {
      alert(message);
      hasShownErrorRef.current = true;
    }
  }, []);

const loadNotifications = useCallback(async () => {
  try {
    const response = await getNotifications();
    const items = response.map(toNotificationItem);

    setNotifications(items);

    if (items.length > 0) {
      setSelectedId((prevSelectedId) => {
        const exists = items.some((item) => item.id === prevSelectedId);

        return exists ? prevSelectedId : items[0].id;
      });
    } else {
      setSelectedId(null);
    }
  } catch {
    showErrorOnce('알림 목록을 불러오지 못했습니다.');
  }
}, [showErrorOnce]);

  const loadUnreadCount = useCallback(async () => {
    try {
      const count = await getUnreadCount();

      setUnreadCount(count);

      window.dispatchEvent(new Event('notification-updated'));
    } catch {
      showErrorOnce('알림 정보를 불러오지 못했습니다.');
    }
  }, [showErrorOnce]);

  useEffect(() => {
    if (!isLoggedIn) {
      return;
    }

    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadNotifications();
    loadUnreadCount();
  }, [isLoggedIn, currentView, loadNotifications, loadUnreadCount]);

  useEffect(() => {
    if (!isLoggedIn) {
      return;
    }

    const timer = window.setInterval(() => {
      loadUnreadCount();
    }, 30000);

    return () => {
      window.clearInterval(timer);
    };
  }, [isLoggedIn, loadUnreadCount]);

  const handleSelectNotification = async (id: number) => {
    try {
      setSelectedId(id);

      const target = notifications.find((item) => item.id === id);

      if (target && !target.isRead) {
        await readNotification(id);

        setNotifications((prev) =>
          prev.map((item) =>
            item.id === id ? { ...item, isRead: true } : item
          )
        );

        await loadUnreadCount();
        await loadNotifications();
      }
    } catch {
      alert('알림 읽음 처리에 실패했습니다.');
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

          <section className="page-content">
            {currentView === 'notifications' ? (
              <div className="notification-section">
                <div className="page-head">
                  <div>
                    <h1>알림함</h1>
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
                      {notifications.length === 0 ? (
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
                        onChange={(event) =>
                          setPushEnabled(event.target.checked)
                        }
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
                </div>
              </div>
            )}
          </section>
        </div>
      </main>
    </div>
  );
}

export default NotificationPage;