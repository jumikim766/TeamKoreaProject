import { useMemo, useState } from 'react';
import Header from '../components/Header';
import Navbar from '../components/Navbar';
import '../styles/Dashboard.css';

type ThemeMode = 'light' | 'dark';
type MailViewMode = 'my-mailbox' | 'mail-connect';

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

interface MailMessage {
  id: number;
  account: string;
  senderName: string;
  senderEmail: string;
  receiverEmail: string;
  subject: string;
  preview: string;
  date: string;
  risk: '매우 위험' | '위험' | '주의' | '안전';
  content: string;
}

interface MailPageProps {
  theme: ThemeMode;
  currentView: MailViewMode;
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

const mailboxAccounts = ['1234@5678.com', '8765@4321.com', 'abcd@efgh.com'];

const mailMessages: MailMessage[] = [
  {
    id: 1,
    account: '1234@5678.com',
    senderName: 'XXX',
    senderEmail: 'abcd@efgh.com',
    receiverEmail: '1234@5678.com',
    subject: '팀코리아 화면정의서',
    preview: '@@@',
    date: '2026년 3월 25일 (수) 12:34',
    risk: '주의',
    content: '@@@',
  },
  {
    id: 2,
    account: '1234@5678.com',
    senderName: '보안팀',
    senderEmail: 'security@company.com',
    receiverEmail: '1234@5678.com',
    subject: 'URL 탐지 결과 공유',
    preview: '오늘 수집된 위험 URL 현황을 확인해주세요.',
    date: '2026년 3월 25일 (수) 10:20',
    risk: '위험',
    content:
      '오늘 수집된 위험 URL 현황을 확인해주세요. 분류 기준에 따라 높은 위험도의 링크가 탐지되었습니다.',
  },
  {
    id: 3,
    account: '8765@4321.com',
    senderName: '관리자',
    senderEmail: 'admin@service.com',
    receiverEmail: '8765@4321.com',
    subject: '계정 연동 상태 안내',
    preview: '이메일 연동 상태가 정상입니다.',
    date: '2026년 3월 24일 (화) 16:02',
    risk: '안전',
    content: '이메일 연동 상태가 정상입니다. 현재 동기화가 문제없이 진행 중입니다.',
  },
];

function MailPage({
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
}: MailPageProps) {
  const [selectedAccount, setSelectedAccount] = useState(mailboxAccounts[0]);
  const [selectedMailId, setSelectedMailId] = useState<number | null>(null);
  const [connectEmail, setConnectEmail] = useState('');
  const [connectEmailError, setConnectEmailError] = useState('');
  const [connectedEmails, setConnectedEmails] = useState(mailboxAccounts);

  const filteredMessages = useMemo(
    () => mailMessages.filter((message) => message.account === selectedAccount),
    [selectedAccount],
  );

  const selectedMail =
    filteredMessages.find((message) => message.id === selectedMailId) ?? filteredMessages[0] ?? null;

  const isValidEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(connectEmail);
  const canConnect = isValidEmail && !connectedEmails.includes(connectEmail);

  const handleDuplicateCheck = () => {
    if (!connectEmail.trim()) {
      setConnectEmailError('이메일 주소를 정확하게 입력해 주세요.');
      return;
    }

    if (!isValidEmail) {
      setConnectEmailError('이메일 주소를 정확하게 입력해 주세요.');
      return;
    }

    if (connectedEmails.includes(connectEmail)) {
      setConnectEmailError('');
      alert('이미 연동된 이메일입니다.');
      return;
    }

    setConnectEmailError('');
    alert('연동 가능한 이메일입니다.');
  };

  const handleConnectEmail = () => {
    if (!isValidEmail) {
      setConnectEmailError('이메일 주소를 정확하게 입력해 주세요.');
      return;
    }

    if (connectedEmails.includes(connectEmail)) {
      setConnectEmailError('');
      alert('이미 연동된 이메일입니다.');
      return;
    }

    setConnectedEmails((prev) => [...prev, connectEmail]);
    setConnectEmail('');
    setConnectEmailError('');
    alert('이메일이 연동되었습니다.');
  };

  const handleDisconnectEmail = (email: string) => {
    const confirmed = window.confirm('이메일을 해제하시겠습니까?');

    if (!confirmed) {
      return;
    }

    const nextConnectedEmails = connectedEmails.filter((item) => item !== email);

    setConnectedEmails(nextConnectedEmails);

    if (selectedAccount === email) {
      const nextAccount = nextConnectedEmails[0] ?? mailboxAccounts[0];
      setSelectedAccount(nextAccount);
      setSelectedMailId(null);
    }

    alert('연동 해제했습니다.');
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
              <div className="page-side-title">메일함</div>

              <button
                className={
                  currentView === 'my-mailbox' ? 'side-menu-button is-active' : 'side-menu-button'
                }
                onClick={() => onNavigate('my-mailbox')}
                type="button"
              >
                나의 메일함
              </button>

              <button
                className={
                  currentView === 'mail-connect' ? 'side-menu-button is-active' : 'side-menu-button'
                }
                onClick={() => onNavigate('mail-connect')}
                type="button"
              >
                메일 연동
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            {currentView === 'my-mailbox' ? (
              <div className="mail-section">
                <div className="mail-top-bar">
                  <select
                    className="mail-account-select"
                    value={selectedAccount}
                    onChange={(event) => {
                      setSelectedAccount(event.target.value);
                      setSelectedMailId(null);
                    }}
                  >
                    {connectedEmails.map((email) => (
                      <option key={email} value={email}>
                        {email}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="mail-content-grid">
                  <section className="mail-list-card">
                    <div className="mail-table mail-table-header">
                      <span>보낸 사람</span>
                      <span>메일 제목</span>
                      <span>메일 본문 미리보기</span>
                      <span>날짜 / 시간</span>
                    </div>

                    <div className="mail-table-body">
                      {filteredMessages.map((message) => (
                        <button
                          key={message.id}
                          className={
                            selectedMail?.id === message.id
                              ? 'mail-table-row is-active'
                              : 'mail-table-row'
                          }
                          onClick={() => setSelectedMailId(message.id)}
                          type="button"
                        >
                          <span>
                            <strong>{message.senderName}</strong>
                            <small>{message.senderEmail}</small>
                          </span>
                          <span>{message.subject}</span>
                          <span>{message.preview}</span>
                          <span>{message.date}</span>
                        </button>
                      ))}
                    </div>
                  </section>

                  <section className="mail-detail-card">
                    {selectedMail ? (
                      <>
                        <div className="mail-detail-head">
                          <h2>{selectedMail.subject}</h2>
                          <span className={`risk-badge risk-${selectedMail.risk}`}>
                            {selectedMail.risk}
                          </span>
                        </div>

                        <div className="mail-meta">
                          <p>
                            <strong>보낸 사람 :</strong> {selectedMail.senderName} (
                            {selectedMail.senderEmail})
                          </p>
                          <p>
                            <strong>받는 사람 :</strong> {selectedMail.receiverEmail}
                          </p>
                          <p>
                            <strong>날짜 :</strong> {selectedMail.date}
                          </p>
                        </div>

                        <div className="mail-divider" />

                        <div className="mail-body">
                          <p>{selectedMail.content}</p>
                        </div>
                      </>
                    ) : (
                      <div className="mail-empty-state">
                        <h3>메일이 없습니다.</h3>
                        <p>선택한 계정의 메일이 없으면 이 영역에 빈 상태가 표시됩니다.</p>
                      </div>
                    )}
                  </section>
                </div>
              </div>
            ) : (
              <div className="mail-section">
                <div className="mypage-head">
                  <p className="eyebrow"></p>
                  <h1>이메일 연동하기</h1>
                </div>

                <div className="mail-connect-box">
                  <input
                    className="mail-connect-input"
                    placeholder="연동하실 이메일을 입력하세요."
                    type="email"
                    value={connectEmail}
                    onChange={(event) => {
                      setConnectEmail(event.target.value);
                      setConnectEmailError('');
                    }}
                  />

                  <button className="secondary-button" onClick={handleDuplicateCheck} type="button">
                    중복 확인
                  </button>

                  <button
                    className={`primary-button ${!canConnect ? 'is-disabled' : ''}`}
                    disabled={!canConnect}
                    onClick={handleConnectEmail}
                    type="button"
                  >
                    연동하기
                  </button>
                </div>

                {connectEmailError && <p className="field-error">{connectEmailError}</p>}

                <div className="mail-connect-divider" />

                <div className="connected-mail-section">
                  <h2>연동된 이메일</h2>

                  <div className="connected-mail-list">
                    {connectedEmails.map((email) => (
                      <div key={email} className="connected-mail-row">
                        <span>{email}</span>
                        <button
                          className="secondary-button small-button"
                          onClick={() => handleDisconnectEmail(email)}
                          type="button"
                        >
                          연동 해제
                        </button>
                      </div>
                    ))}
                  </div>
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

export default MailPage;