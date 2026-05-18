import { useEffect, useState } from "react";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import "../styles/MailPage.css";
import { getRiskClassName, getRiskLabel } from "../utils/riskLevel";

import {
  getEmailAccount,
  createEmailAccount,
  deleteEmailAccount,
  getEmail,
  getEmailDetail,
} from "../api/mailApi";

import type {
  EmailAccount,
  CreateEmailAccountRequest,
  EmailListItem,
  EmailListResponse,
  EmailDetail,
} from "../api/mailApi";

type ThemeMode = "light" | "dark";
type MailViewMode = "my-mailbox" | "mail-connect";

type PageViewTarget =
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

const mailboxAccounts = ["1234@5678.com", "8765@4321.com", "abcd@efgh.com"];

function MailPage({
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
}: MailPageProps) {
  const [selectedAccount, setSelectedAccount] = useState(mailboxAccounts[0]);
  const [selectedMailId, setSelectedMailId] = useState<number | null>(null);
  const [connectEmail, setConnectEmail] = useState("");
  const [selectedProvider, setSelectedProvider] = useState<
    "GMAIL" | "NAVER" | "DAUM" | "OUTLOOK" | "CUSTOM"
  >("GMAIL");
  const [connectEmailError, setConnectEmailError] = useState("");
  const [connectedEmails, setConnectedEmails] = useState(mailboxAccounts);
  const [emailAccounts, setEmailAccounts] = useState<EmailAccount[]>([]);
  const [emails, setEmails] = useState<EmailListItem[]>([]);
  const [selectedEmailDetail, setSelectedEmailDetail] =
    useState<EmailDetail | null>(null);

  useEffect(() => {
    const fetchEmailAccounts = async () => {
      try {
        const accounts = await getEmailAccount();
        setEmailAccounts(accounts);
      } catch (error) {
        console.error("이메일 계정 목록 조회 실패:", error);
      }
    };
    fetchEmailAccounts();
  }, []);

  useEffect(() => {
    const fetchEmails = async () => {
      try {
        const data: EmailListResponse = await getEmail({ page: 0, size: 20 });
        setEmails(data.emails);
      } catch (error) {
        console.error("이메일 목록 조회 실패:", error);
      }
    };
    fetchEmails();
  }, []);

  useEffect(() => {
    if (selectedMailId === null) return;

    const fetchEmailDetail = async () => {
      try {
        const detail = await getEmailDetail(selectedMailId);
        setSelectedEmailDetail(detail);
      } catch (error) {
        console.error("이메일 상세 조회 실패:", error);
      }
    };
    fetchEmailDetail();
  }, [selectedMailId]);

  const filteredMessages = emails;

  const selectedMail =
    filteredMessages.find((message) => message.emailId === selectedMailId) ??
    filteredMessages[0] ??
    null;

  const isValidEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(connectEmail);
  const canConnect = isValidEmail && !connectedEmails.includes(connectEmail);

  const handleDuplicateCheck = () => {
    if (!connectEmail.trim()) {
      setConnectEmailError("이메일 주소를 정확하게 입력해 주세요.");
      return;
    }

    if (!isValidEmail) {
      setConnectEmailError("이메일 주소를 정확하게 입력해 주세요.");
      return;
    }

    if (connectedEmails.includes(connectEmail)) {
      setConnectEmailError("");
      alert("이미 연동된 이메일입니다.");
      return;
    }

    setConnectEmailError("");
    alert("연동 가능한 이메일입니다.");
  };

  const handleConnectEmail = async () => {
    if (!isValidEmail) {
      setConnectEmailError("이메일 주소를 정확하게 입력해 주세요.");
      return;
    }

    if (connectedEmails.includes(connectEmail)) {
      setConnectEmailError("");
      alert("이미 연동된 이메일입니다.");
      return;
    }

    try {
      const request: CreateEmailAccountRequest = {
        provider: selectedProvider,
        email: connectEmail,
        loginId: connectEmail,
        secret: "temp-password",
      };

      await createEmailAccount(request);

      setConnectedEmails((prev) => [...prev, connectEmail]);
      setConnectEmail("");
      setConnectEmailError("");
      alert("이메일이 연동되었습니다.");
    } catch (error) {
      console.error("이메일 연동 실패:", error);
    }
  };

  const handleDisconnectEmail = async (email: string) => {
    const confirmed = window.confirm("이메일을 해제하시겠습니까?");

    if (!confirmed) {
      return;
    }

    try {
      const targetAccount = emailAccounts.find(
        (account) => account.email === email,
      );

      if (targetAccount) {
        await deleteEmailAccount(targetAccount.accountId);
      }
      const nextConnectedEmails = connectedEmails.filter(
        (item) => item !== email,
      );

      setConnectedEmails(nextConnectedEmails);

      if (selectedAccount === email) {
        const nextAccount = nextConnectedEmails[0] ?? "";
        setSelectedAccount(nextAccount);
        setSelectedMailId(null);
      }

      alert("연동 해제했습니다.");
    } catch (error) {
      console.error("이메일 연동 해제 실패:", error);
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
              <div className="page-side-title">메일함</div>

              <button
                className={
                  currentView === "my-mailbox"
                    ? "side-menu-button is-active"
                    : "side-menu-button"
                }
                onClick={() => onNavigate("my-mailbox")}
                type="button"
              >
                나의 메일함
              </button>

              <button
                className={
                  currentView === "mail-connect"
                    ? "side-menu-button is-active"
                    : "side-menu-button"
                }
                onClick={() => onNavigate("mail-connect")}
                type="button"
              >
                메일 연동
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            {currentView === "my-mailbox" ? (
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
                          key={message.emailId}
                          className={
                            selectedMail?.emailId === message.emailId
                              ? "mail-table-row is-active"
                              : "mail-table-row"
                          }
                          onClick={() => setSelectedMailId(message.emailId)}
                          type="button"
                        >
                          <span>
                            <strong>{message.senderName}</strong>
                          </span>
                          <span>{message.subject}</span>
                          <span>{message.previewText}</span>
                          <span>{message.receivedAt}</span>
                        </button>
                      ))}
                    </div>
                  </section>

                  <section className="mail-detail-card">
                    {selectedMail ? (
                      <>
                        <div className="mail-detail-head">
                          <h2>{selectedMail.subject}</h2>
                          <span
                            className={`risk-badge ${getRiskClassName(selectedEmailDetail?.riskLevel)}`}
                          >
                            {getRiskLabel(selectedEmailDetail?.riskLevel)}
                          </span>
                        </div>

                        <div className="mail-meta">
                          <p>
                            <strong>보낸 사람 :</strong>{" "}
                            {selectedEmailDetail?.senderName} (
                            {selectedEmailDetail?.senderEmail})
                          </p>
                          <p>
                            <strong>받는 사람 :</strong>{" "}
                            {selectedEmailDetail?.receiverEmail}
                          </p>
                          <p>
                            <strong>날짜 :</strong>{" "}
                            {selectedEmailDetail?.receivedAt}
                          </p>
                        </div>

                        <div className="mail-divider" />

                        <div className="mail-body">
                          <p>{selectedEmailDetail?.bodyText}</p>
                          <p>
                            <strong>보낸 사람 :</strong>{" "}
                            {selectedEmailDetail?.senderName} (
                            {selectedEmailDetail?.senderEmail})
                          </p>
                          <p>
                            <strong>받는 사람 :</strong>{" "}
                            {selectedEmailDetail?.receiverEmail}
                          </p>
                          <p>
                            <strong>날짜 :</strong>{" "}
                            {selectedEmailDetail?.receivedAt}
                          </p>
                        </div>
                      </>
                    ) : (
                      <div className="mail-empty-state">
                        <h3>메일이 없습니다.</h3>
                        <p>
                          선택한 계정의 메일이 없으면 이 영역에 빈 상태가
                          표시됩니다.
                        </p>
                      </div>
                    )}
                  </section>
                </div>
              </div>
            ) : (
              <div className="mail-section">
                <div className="page-head">
                  <p className="eyebrow"></p>
                  <h1>이메일 연동하기</h1>
                </div>

                <div className="mail-connect-box">
                  <select
                    className="mail-provider-select"
                    value={selectedProvider}
                    onChange={(event) =>
                      setSelectedProvider(
                        event.target.value as
                          | "GMAIL"
                          | "NAVER"
                          | "DAUM"
                          | "OUTLOOK"
                          | "CUSTOM",
                      )
                    }
                  >
                    <option value="GMAIL">GMAIL</option>
                    <option value="NAVER">NAVER</option>
                    <option value="DAUM">DAUM</option>
                    <option value="OUTLOOK">OUTLOOK</option>
                    <option value="CUSTOM">CUSTOM</option>
                  </select>

                  <input
                    className="mail-connect-input"
                    placeholder="연동하실 이메일을 입력하세요."
                    type="email"
                    value={connectEmail}
                    onChange={(event) => {
                      setConnectEmail(event.target.value);
                      setConnectEmailError("");
                    }}
                  />

                  <button
                    className="secondary-button"
                    type="button"
                    onClick={handleDuplicateCheck}
                  >
                    중복 확인
                  </button>

                  <button
                    className={`primary-button ${!canConnect ? "is-disabled" : ""}`}
                    disabled={!canConnect}
                    onClick={handleConnectEmail}
                    type="button"
                  >
                    연동하기
                  </button>
                </div>

                {connectEmailError && (
                  <p className="field-error">{connectEmailError}</p>
                )}

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
        <button type="button" onClick={() => onNavigate("service-info")}>
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

export default MailPage;
