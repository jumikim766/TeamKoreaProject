import { useMemo, useState } from 'react';
import type { ViewMode } from '../App';
import Header from '../components/Header';
import Navbar from '../components/Navbar';
import { readNotification } from '../api/notificationApi';
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

const initialNotifications: NotificationItem[] = [
  {
    id: 1,
    title: '고위험 URL이 탐지되었습니다.',
    summary:
      '메일 기반 수집 데이터에서 매우 위험 URL 3건이 추가 탐지되었습니다.',
    content:
      '메일 기반 수집 데이터에서 매우 위험 URL 3건이 추가 탐지되었습니다. URL 관리 페이지에서 상세 링크와 위험도를 확인해 주세요.',
    date: '2026. 06. 10. 오후 2:14',
    type: '위험 URL',
    isRead: false,
  },
  {
    id: 2,
    title: '이메일 연동 상태가 정상입니다.',
    summary: '연동된 메일 계정의 동기화가 정상적으로 완료되었습니다.',
    content:
      '연동된 메일 계정의 동기화가 정상적으로 완료되었습니다. 마지막 점검 시간은 오후 1:42이며 현재 오류는 없습니다.',
    date: '2026. 06. 10. 오후 1:42',
    type: '메일 연동',
    isRead: true,
  },
  {
    id: 3,
    title: '신고 접수 건 처리 상태가 변경되었습니다.',
    summary: '사용자가 신고한 URL 1건이 검토 완료 상태로 변경되었습니다.',
    content:
      '사용자가 신고한 URL 1건이 검토 완료 상태로 변경되었습니다. 신고 내역 페이지에서 조치 결과를 확인할 수 있습니다.',
    date: '2026. 06. 09. 오후 7:18',
    type: '신고 처리',
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
  const [notifications, setNotifications] =
    useState<NotificationItem[]>(initialNotifications);

  const [selectedId, setSelectedId] = useState<number | null>(
    initialNotifications[0]?.id ?? null
  );

  const [pushEnabled, setPushEnabled] = useState(true);

  const unreadCount = useMemo(() => {
    return notifications.filter((item) => !item.isRead).length;
  }, [notifications]);

  const selectedNotification = useMemo(() => {
    return notifications.find((item) => item.id === selectedId) ?? null;
  }, [notifications, selectedId]);

  const handleSelectNotification = async (id: number) => {
    setSelectedId(id);

    const target = notifications.find((item) => item.id === id);

    if (!target || target.isRead) {
      return;
    }

    setNotifications((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, isRead: true } : item
      )
    );

    try {
      await readNotification(id);
    } catch {
      console.warn('백엔드 읽음 처리 API 호출 실패');
    }
  };

  const handleDeleteNotification = (id: number) => {
    const nextNotifications = notifications.filter((item) => item.id !== id);

    setNotifications(nextNotifications);

    if (selectedId === id) {
      setSelectedId(nextNotifications[0]?.id ?? null);
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
                        <div className="notification-empty-state">
                          <div className="notification-empty-icon">🔔</div>

                          <strong>새로운 알림이 없습니다</strong>

                          <p>
                            위험 URL 탐지, 신고 처리, 메일 연동 알림이
                            생기면 이곳에 표시됩니다.
                          </p>
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

                        <div style={{ marginTop: '20px' }}>
                          <button
                            className="secondary-button"
                            type="button"
                            onClick={() =>
                              handleDeleteNotification(selectedNotification.id)
                            }
                          >
                            알림 삭제하기
                          </button>
                        </div>
                      </>
                    ) : (
                      <div className="notification-empty-state">
                        <div className="notification-empty-icon">🔔</div>

                        <strong>새로운 알림이 없습니다</strong>

                        <p>
                          위험 URL 탐지, 신고 처리, 메일 연동 알림이 생기면
                          이곳에 표시됩니다.
                        </p>
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