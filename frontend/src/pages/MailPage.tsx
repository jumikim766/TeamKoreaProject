import { useEffect, useState } from "react";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import "../styles/MailPage.css";
import { getRiskClassName, getRiskLabel } from "../utils/riskLevel";

// API 함수
import {
  getEmailAccount,
  createEmailAccount,
  deleteEmailAccount,
  getEmail,
  getEmailDetail,
  syncEmailAccount,
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
  const [selectedAccountId, setSelectedAccountId] = useState<number | null>(
    null,
  );
  const [selectedMailId, setSelectedMailId] = useState<number | null>(null);
  const [connectEmail, setConnectEmail] = useState("");
  const [loginId, setLoginId] = useState("");
  const [secret, setSecret] = useState("");
  const [selectedProvider, setSelectedProvider] = useState<
    "GMAIL" | "NAVER" | "DAUM" | "OUTLOOK" | "CUSTOM"
  >("GMAIL");
  const [connectEmailError, setConnectEmailError] = useState("");
  const [connectedEmails, setConnectedEmails] = useState<string[]>([]);
  const [emailAccounts, setEmailAccounts] = useState<EmailAccount[]>([]);
  const [emails, setEmails] = useState<EmailListItem[]>([]);
  const [selectedEmailDetail, setSelectedEmailDetail] =
    useState<EmailDetail | null>(null);
  const [imapHost, setImapHost] = useState("");
  const [imapPort, setImapPort] = useState("");
  const [syncingAccountId, setSyncingAccountId] = useState<number | null>(null);

  useEffect(() => {
    const fetchEmailAccounts = async () => {
      try {
        const accounts = await getEmailAccount();

        // 이메일 계정 state 저장
        setEmailAccounts(accounts);

        const accountEmails = accounts.map((account) => account.email);
        setConnectedEmails(accountEmails);

        if (accounts.length > 0) {
          setSelectedAccountId(accounts[0].accountId);
        }
      } catch (error) {
        console.error("이메일 계정 목록 조회 실패:", error);
      }
    };
    fetchEmailAccounts();
  }, []);

  useEffect(() => {
    if (selectedAccountId === null) {
      return;
    }

    const fetchEmails = async () => {
      try {
        const data: EmailListResponse = await getEmail({
          accountId: selectedAccountId,
          page: 0,
          size: 20,
        });

        setEmails(data.emails ?? []);
        setSelectedMailId(null);
        setSelectedEmailDetail(null);
      } catch (error) {
        console.error("이메일 목록 조회 실패:", error);
        setEmails([]);
      }
    };

    fetchEmails();
  }, [selectedAccountId]);

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
    selectedMailId !== null
      ? (filteredMessages.find(
          (message) => message.emailId === selectedMailId,
        ) ?? null)
      : null;

  const isValidEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(connectEmail);
  const canConnect = isValidEmail && !connectedEmails.includes(connectEmail);

  const handleDuplicateCheck = async () => {
    if (!connectEmail.trim() || !isValidEmail) {
      setConnectEmailError("이메일 주소를 정확하게 입력해 주세요.");
      return;
    }

    if (!isValidEmail) {
      setConnectEmailError("이메일 주소를 정확하게 입력해 주세요.");
      return;
    }

    try {
      const accounts = await getEmailAccount();

      const isDuplicate = accounts.some(
        (account) => account.email === connectEmail,
      );

      if (isDuplicate) {
        alert("이미 연동된 이메일입니다.");
        return;
      }

      alert("연동 가능한 이메일입니다.");
    } catch (error) {
      console.error("중복 확인 실패:", error);
    }
  };

  const handleConnectEmail = async () => {
    if (!isValidEmail) {
      setConnectEmailError("이메일 주소를 정확하게 입력해 주세요.");
      return;
    }

    try {
      const request: CreateEmailAccountRequest = {
        provider: selectedProvider,
        email: connectEmail,
        loginId,
        secret,
        ...(selectedProvider === "CUSTOM" && {
          imapHost,
          imapPort: Number(imapPort),
        }),
      };

      await createEmailAccount(request);

      const accounts = await getEmailAccount();
      setEmailAccounts(accounts);

      const accountEmails = accounts.map((account) => account.email);
      setConnectedEmails(accountEmails);

      const connectedAccount = accounts.find(
        (account) => account.email === connectEmail,
      );

      setSelectedAccountId(
        connectedAccount?.accountId ?? accounts[0]?.accountId ?? null,
      );

      setConnectEmail("");
      setLoginId("");
      setSecret("");
      setImapHost("");
      setImapPort("");
      setConnectEmailError("");
      alert("이메일이 연동되었습니다.");
    } catch (error) {
      console.error("이메일 연동 실패:", error);
    }
  };

  const handleSyncEmail = async (email: string) => {
    const targetAccount = emailAccounts.find(
      (account) => account.email === email,
    );

    if (!targetAccount) {
      alert("계정 정보를 찾을 수 없습니다.");
      return;
    }

    try {
      setSyncingAccountId(targetAccount.accountId);

      await syncEmailAccount(targetAccount.accountId);

      const data: EmailListResponse = await getEmail({
        accountId: targetAccount.accountId,
        page: 0,
        size: 20,
      });
      setEmails(data.emails);

      alert("이메일 동기화 완료");
    } catch (error: unknown) {
      if (typeof error === "object" && error !== null && "response" in error) {
        const err = error as {
          response?: {
            status?: number;
            data?: {
              message?: string;
              error?: string;
            };
          };
        };

        console.error("이메일 동기화 실패:", err);
        console.error("응답 상태:", err.response?.status);
        console.error("응답 데이터:", err.response?.data);

        alert(
          err.response?.data?.message ||
            err.response?.data?.error ||
            "이메일 동기화에 실패했습니다.",
        );
      } else {
        console.error("알 수 없는 오류:", error);
        alert("이메일 동기화에 실패했습니다.");
      }
    } finally {
      setSyncingAccountId(null);
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

      if (!targetAccount) {
        alert("이메일 계정을 찾을 수 없습니다.");
        return;
      }

      if (targetAccount) {
        await deleteEmailAccount(targetAccount.accountId);
      }

      const accounts = await getEmailAccount();
      setEmailAccounts(accounts);

      const accountEmails = accounts.map((account) => account.email);
      setConnectedEmails(accountEmails);

      if (selectedAccountId === targetAccount.accountId) {
        const nextAccountId = accounts[0]?.accountId ?? null;

        setSelectedAccountId(nextAccountId);
        setSelectedMailId(null);
        setSelectedEmailDetail(null);
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
                    value={selectedAccountId ?? ""}
                    onChange={(event) => {
                      setSelectedAccountId(Number(event.target.value));
                      setSelectedMailId(null);
                      setSelectedEmailDetail(null);
                    }}
                  >
                    {emailAccounts.length > 0 ? (
                      emailAccounts.map((account) => (
                        <option
                          key={account.accountId}
                          value={account.accountId}
                        >
                          {account.email}
                        </option>
                      ))
                    ) : (
                      <option value="">연동된 이메일이 없습니다.</option>
                    )}
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
                            <small>{message.senderEmail}</small>
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
                          {/* HTML 본문이 있으면 HTML로 보여주고, 없으면 기존 텍스트 본문 표시 */}
                          {selectedEmailDetail?.bodyHtml ? (
                            <div
                              className="mail-body-html"
                              dangerouslySetInnerHTML={{
                                __html: selectedEmailDetail.bodyHtml,
                              }}
                            />
                          ) : (
                            <p>{selectedEmailDetail?.bodyText}</p>
                          )}
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
                  <div className="mail-connect-first-row">
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
                  </div>
                  <input
                    className="mail-auth-input"
                    placeholder="로그인 ID"
                    type="text"
                    value={loginId}
                    onChange={(event) => setLoginId(event.target.value)}
                  />
                  <input
                    className="mail-auth-input"
                    placeholder="비밀번호"
                    type="password"
                    value={secret}
                    onChange={(event) => setSecret(event.target.value)}
                  />

                  {selectedProvider === "CUSTOM" && (
                    <>
                      <input
                        className="mail-auth-input"
                        placeholder="imap host"
                        value={imapHost}
                        onChange={(event) => setImapHost(event.target.value)}
                      />

                      <input
                        className="mail-auth-input"
                        placeholder="imap port"
                        value={imapPort}
                        onChange={(event) => setImapPort(event.target.value)}
                      />
                    </>
                  )}

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
                    {connectedEmails.length > 0 ? (
                      connectedEmails.map((email) => (
                        <div key={email} className="connected-mail-row">
                          <span>{email}</span>
                          <div className="mail-button-group">
                            <button
                              className="secondary-button small-button"
                              onClick={() => handleSyncEmail(email)}
                              disabled={
                                syncingAccountId ===
                                emailAccounts.find(
                                  (account) => account.email === email,
                                )?.accountId
                              }
                              type="button"
                            >
                              {syncingAccountId ===
                              emailAccounts.find(
                                (account) => account.email === email,
                              )?.accountId
                                ? "동기화 중..."
                                : "동기화"}
                            </button>
                            <button
                              className="secondary-button small-button"
                              onClick={() => handleDisconnectEmail(email)}
                              type="button"
                            >
                              연동 해제
                            </button>
                          </div>
                        </div>
                      ))
                    ) : (
                      <p className="connected-mail-empty">
                        연동된 이메일이 없습니다.
                      </p>
                    )}
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
