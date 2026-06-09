import { useEffect, useState } from "react";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import {
  changePassword,
  deleteMe,
  getMe,
  sendEmailChangeCode,
  updateMe,
  verifyEmailChangeCode,
} from "../api/userApi";
import type { UserMe, UserUpdatePayload } from "../api/userApi";
import { getErrorMessage } from "../api/errorMessage";
import { login } from "../api/authApi";
import { clearAccessToken, saveAccessToken } from "../utils/token";
import type { ViewMode } from "../App";
import PasswordRules from "../components/auth/PasswordRules";
import "../styles/MyPage.css";
import "../styles/AuthPage.css";

type ThemeMode = "light" | "dark";
type MyPageTab = "profile" | "password" | "withdraw";
type Gender = "" | "MALE" | "FEMALE" | "OTHER";
type Provider = "LOCAL" | "GOOGLE" | "NAVER";

interface MyPageProps {
  theme: ThemeMode;
  isLoggedIn?: boolean;
  userName?: string;
  onLogout?: () => void;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
  onGoNotifications?: () => void;
  unreadCount?: number;
  onNavigate: (view: ViewMode) => void;
}


function EyeIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

function EyeOffIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49" />
      <path d="M14.084 14.158a3 3 0 0 1-4.242-4.242" />
      <path d="M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143" />
      <path d="m2 2 20 20" />
    </svg>
  );
}

function MyPage({
  theme,
  isLoggedIn = false,
  userName = "",
  onLogout,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onGoNotifications,
  unreadCount = 0,
  onNavigate,
}: MyPageProps) {
  const [tab, setTab] = useState<MyPageTab>("profile");
  const [originalUser, setOriginalUser] = useState<UserMe | null>(null);

  const [name, setName] = useState("");
  const [username, setUsername] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");
  const [provider, setProvider] = useState<Provider>("LOCAL");
  const [gender, setGender] = useState<Gender>("");
  const [age, setAge] = useState("");

  const [isEmailVerifyModalOpen, setIsEmailVerifyModalOpen] = useState(false);
  const [emailVerifyCode, setEmailVerifyCode] = useState("");
  const [emailVerifyTimeLeft, setEmailVerifyTimeLeft] = useState(180);
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [emailMessage, setEmailMessage] = useState("");

  const [currentPassword, setCurrentPassword] = useState("");
  const [nextPassword, setNextPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNextPassword, setShowNextPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [withdrawPassword, setWithdrawPassword] = useState("");
  const [showWithdrawPassword, setShowWithdrawPassword] = useState(false);
  const [isWithdrawPasswordVerified, setIsWithdrawPasswordVerified] = useState(false);
  const [withdrawMessage, setWithdrawMessage] = useState("");

  const isSocialUser = provider !== "LOCAL";

  const applyUserToState = (user: UserMe) => {
    setOriginalUser(user);
    setName(user.name ?? "");
    setUsername(user.username ?? "");
    setEmail(user.email ?? "");
    setPhone(user.phoneMasked ?? "");
    setProvider(user.provider);
    setGender(
      user.gender === "MALE" || user.gender === "FEMALE" || user.gender === "OTHER"
        ? user.gender
        : ""
    );
    setAge(user.age === null || user.age === undefined ? "" : String(user.age));
    setIsEmailVerified(false);
    setEmailVerifyCode("");
    setEmailMessage("");
  };

  useEffect(() => {
    const loadMyInfo = async () => {
      try {
        const res = await getMe();
        const user = res.data.data;
        if (!user) return;
        applyUserToState(user);
      } catch (error) {
        alert(getErrorMessage(error, "회원 정보를 불러오지 못했습니다."));
      }
    };

    loadMyInfo();
  }, []);

  useEffect(() => {
    if (!isEmailVerifyModalOpen || isEmailVerified) return;
    if (emailVerifyTimeLeft <= 0) return;

    const timer = window.setInterval(() => {
      setEmailVerifyTimeLeft((prev) => prev - 1);
    }, 1000);

    return () => window.clearInterval(timer);
  }, [isEmailVerifyModalOpen, isEmailVerified, emailVerifyTimeLeft]);

  const formatEmailVerifyTime = (seconds: number) => {
    const minute = Math.floor(seconds / 60);
    const second = seconds % 60;
    return `${minute}:${String(second).padStart(2, "0")}`;
  };

  const phoneOnlyNumber = phone.replace(/-/g, "");
  const isPhoneMasked = phone.includes("*");
  const isEmailChanged = Boolean(originalUser && email.trim() !== originalUser.email);
  const isEmailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  const isPhoneValid = phone.trim() === "" || isPhoneMasked || /^010\d{8}$/.test(phoneOnlyNumber);
  const isAgeValid = age.trim() === "" || /^[0-9]+$/.test(age);
  const isProfileValid =
    Boolean(originalUser) &&
    isPhoneValid &&
    isAgeValid &&
    (!isEmailChanged || (isEmailValid && isEmailVerified));

  const hasPasswordLetter = /[a-zA-Z]/.test(nextPassword);
  const hasPasswordNumber = /[0-9]/.test(nextPassword);
  const hasPasswordSpecial = /[^a-zA-Z0-9]/.test(nextPassword);
  const hasPasswordLength = nextPassword.length >= 8;
  const isNextPasswordValid = hasPasswordLetter && hasPasswordNumber && hasPasswordSpecial && hasPasswordLength;
  const isPasswordValid =
    currentPassword.trim().length >= 1 &&
    isNextPasswordValid &&
    confirmPassword.trim().length >= 1 &&
    nextPassword === confirmPassword;

  const canCheckWithdrawPassword = isSocialUser || withdrawPassword.trim().length >= 1;
  const canWithdraw = isSocialUser || isWithdrawPasswordVerified;

  const handleEmailChangeCodeSend = async () => {
    if (isSocialUser) return;

    if (!isEmailValid) {
      setEmailMessage("이메일 형식이 올바르지 않습니다.");
      return;
    }

    if (!isEmailChanged) {
      setEmailMessage("현재 이메일과 다른 이메일을 입력해주세요.");
      return;
    }

    try {
      await sendEmailChangeCode({ email: email.trim() });
      setIsEmailVerified(false);
      setEmailVerifyCode("");
      setEmailVerifyTimeLeft(180);
      setEmailMessage("인증코드가 발송되었습니다.");
      setIsEmailVerifyModalOpen(true);
    } catch (error) {
      setIsEmailVerified(false);
      setEmailMessage(getErrorMessage(error, "인증코드 발송에 실패했습니다."));
    }
  };

  const handleEmailChangeCodeResend = async () => {
    try {
      await sendEmailChangeCode({ email: email.trim() });
      setIsEmailVerified(false);
      setEmailVerifyCode("");
      setEmailVerifyTimeLeft(180);
      setEmailMessage("인증코드가 재발송되었습니다.");
    } catch (error) {
      setEmailMessage(getErrorMessage(error, "인증코드 재발송에 실패했습니다."));
    }
  };

  const handleEmailChangeCodeVerify = async () => {
    try {
      await verifyEmailChangeCode({ email: email.trim(), code: emailVerifyCode.trim() });
      setIsEmailVerified(true);
      setEmailMessage("이메일 인증이 완료되었습니다.");
      alert("이메일 인증이 완료되었습니다.");
      setIsEmailVerifyModalOpen(false);
    } catch (error) {
      setIsEmailVerified(false);
      alert(getErrorMessage(error, "인증코드가 올바르지 않습니다."));
    }
  };

  const handleGenderClick = (nextGender: Gender) => {
    setGender((current) => (current === nextGender ? "" : nextGender));
  };

  const handleProfileSubmit = async () => {
    if (!originalUser || !isProfileValid) return;

    const payload: UserUpdatePayload = {};

    if (!isSocialUser && isEmailChanged) {
      payload.email = email.trim();
    }

    if (!isPhoneMasked && phone.trim() !== "" && phoneOnlyNumber !== originalUser.phoneMasked) {
      payload.phone = phoneOnlyNumber;
    }

    const originalGender = originalUser.gender ?? "";
    if (gender !== originalGender && gender !== "") {
      payload.gender = gender;
    }

    const ageValue = age.trim() === "" ? undefined : Number(age);
    if (ageValue !== undefined && ageValue !== originalUser.age) {
      payload.age = ageValue;
    }

    if (Object.keys(payload).length === 0) {
      alert("변경된 내용이 없습니다.");
      return;
    }

    try {
      await updateMe(payload);
      const res = await getMe();
      const updatedUser = res.data.data;
      alert("회원 정보가 수정되었습니다.");

      if (updatedUser) {
        applyUserToState(updatedUser);
      }
    } catch (error) {
      alert(getErrorMessage(error, "회원 정보 수정에 실패했습니다."));
    }
  };

  const handlePasswordSubmit = async () => {
    if (!isPasswordValid) return;

    try {
      await changePassword(currentPassword, nextPassword);
      alert("비밀번호가 변경되었습니다.");
      setCurrentPassword("");
      setNextPassword("");
      setConfirmPassword("");
    } catch (error) {
      alert(getErrorMessage(error, "비밀번호 변경에 실패했습니다."));
    }
  };

  const handleWithdrawPasswordCheck = async () => {
    if (!originalUser || !canCheckWithdrawPassword) return;

    if (isSocialUser) {
      setIsWithdrawPasswordVerified(true);
      setWithdrawMessage("소셜 로그인 계정은 비밀번호 확인 없이 탈퇴할 수 있습니다.");
      return;
    }

    try {
      const res = await login(username.trim(), withdrawPassword);
      const accessToken = res.data.data?.accessToken;

      if (accessToken) {
        saveAccessToken(accessToken);
      }

      setIsWithdrawPasswordVerified(true);
      setWithdrawMessage("비밀번호 확인이 완료되었습니다.");
    } catch (error) {
      setIsWithdrawPasswordVerified(false);
      setWithdrawMessage(getErrorMessage(error, "비밀번호가 올바르지 않습니다."));
    }
  };

  const handleWithdrawSubmit = async () => {
    if (!canWithdraw) return;

    const confirmed = window.confirm("정말 회원탈퇴를 진행하시겠습니까?");
    if (!confirmed) return;

    try {
      await deleteMe(isSocialUser ? null : withdrawPassword);
      clearAccessToken();
      localStorage.removeItem("userName");
      alert("회원 탈퇴가 완료되었습니다.");
      onNavigate("login");
    } catch (error) {
      alert(getErrorMessage(error, "회원 탈퇴에 실패했습니다."));
    }
  };

  const handleCancelProfile = () => {
    if (originalUser) applyUserToState(originalUser);
  };

  return (
    <div className="dashboard-shell">
      <Header
        currentView="mypage"
        theme={theme}
        isLoggedIn={isLoggedIn}
        userName={userName}
        onLogout={onLogout}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onGoMyPage={onGoMyPage}
        onGoNotifications={onGoNotifications}
        unreadCount={unreadCount}
        onToggleTheme={onToggleTheme}
      />

      <Navbar currentView="mypage" onNavigate={onNavigate} />

      <main className="page-main">
        <div className="page-layout">
          <aside className="page-sidebar">
            <button className="back-button" onClick={onGoHome} type="button">뒤로가기</button>

            <div className="page-side-card">
              <div className="page-side-title">마이페이지</div>
              <button className={tab === "profile" ? "side-menu-button is-active" : "side-menu-button"} onClick={() => setTab("profile")} type="button">회원 정보 수정</button>
              {!isSocialUser && (
                <button className={tab === "password" ? "side-menu-button is-active" : "side-menu-button"} onClick={() => setTab("password")} type="button">비밀번호 변경</button>
              )}
              <button className={tab === "withdraw" ? "side-menu-button is-active" : "side-menu-button"} onClick={() => setTab("withdraw")} type="button">회원탈퇴</button>
            </div>
          </aside>

          <section className="page-content-card">
            {tab === "withdraw" ? (
              <div className="mypage-section withdraw-section">
                <div className="mypage-head">
                  <h1>회원탈퇴</h1>
                  <p className="mypage-sub-text">회원탈퇴 전 계정 정보를 확인하고 현재 비밀번호를 입력해 주세요.</p>
                </div>

                <div className="mypage-form-grid">
                  <label className="mypage-row">
                    <span>아이디</span>
                    <input value={username} type="text" disabled className="readonly-input" />
                  </label>

                  <label className="mypage-row">
                    <span>이메일</span>
                    <input value={email} type="email" disabled className="readonly-input" />
                  </label>

                  <label className="mypage-row">
                    <span>현재 비밀번호</span>
                    <div className="mypage-inline-field withdraw-password-field">
                      <div className="mypage-password-input-wrap">
                        <input
                          value={withdrawPassword}
                          onChange={(e) => {
                            setWithdrawPassword(e.target.value);
                            setIsWithdrawPasswordVerified(false);
                            setWithdrawMessage("");
                          }}
                          type={showWithdrawPassword ? "text" : "password"}
                          placeholder={isSocialUser ? "소셜 로그인 계정입니다" : "현재 비밀번호를 입력해 주세요"}
                          disabled={isSocialUser}
                        />
                        {!isSocialUser && (
                          <button
                            type="button"
                            className="mypage-eye-button"
                            onClick={() => setShowWithdrawPassword((prev) => !prev)}
                            aria-label={showWithdrawPassword ? "비밀번호 숨기기" : "비밀번호 보기"}
                          >
                            {showWithdrawPassword ? <EyeOffIcon /> : <EyeIcon />}
                          </button>
                        )}
                      </div>
                      <button
                        type="button"
                        className="mypage-check-button"
                        disabled={!canCheckWithdrawPassword}
                        onClick={handleWithdrawPasswordCheck}
                      >
                        비밀번호 확인
                      </button>
                    </div>
                    {withdrawMessage && (
                      <p className={isWithdrawPasswordVerified ? "mypage-success-text" : "mypage-error-text"}>
                        {withdrawMessage}
                      </p>
                    )}
                  </label>
                </div>

                <div className="mypage-actions mypage-actions-right">
                  <button className="secondary-button" type="button" onClick={() => setTab("profile")}>취소</button>
                  <button
                    className={`mypage-danger-button ${canWithdraw ? "is-active" : ""}`}
                    disabled={!canWithdraw}
                    type="button"
                    onClick={handleWithdrawSubmit}
                  >
                    탈퇴하기
                  </button>
                </div>
              </div>
            ) : tab === "profile" || isSocialUser ? (
              <div className="mypage-section">
                <div className="mypage-head"><h1>회원 정보 수정</h1></div>

                <div className="mypage-form-grid">
                  <label className="mypage-row">
                    <span>이름</span>
                    <input value={name} type="text" placeholder="이름" disabled className="readonly-input" />
                  </label>

                  <label className="mypage-row">
                    <span>아이디</span>
                    <input value={username} type="text" placeholder="아이디" disabled className="readonly-input" />
                  </label>

                  <label className="mypage-row">
                    <span>전화번호</span>
                    <input value={phone} onChange={(e) => setPhone(e.target.value.replace(/\s/g, ""))} type="text" placeholder="선택 입력: 01012345678" />
                  </label>

                  <label className="mypage-row">
                    <span>이메일</span>
                    <div className="mypage-inline-field">
                      <input
                        value={email}
                        onChange={(e) => {
                          setEmail(e.target.value.replace(/\s/g, ""));
                          setIsEmailVerified(false);
                          setEmailMessage("");
                        }}
                        type="email"
                        placeholder="example@email.com"
                        disabled={isSocialUser}
                      />
                      {!isSocialUser && (
                        <button type="button" className="mypage-check-button" onClick={handleEmailChangeCodeSend}>이메일 인증</button>
                      )}
                    </div>
                    {emailMessage && <p className={isEmailVerified ? "mypage-success-text" : "mypage-error-text"}>{emailMessage}</p>}
                  </label>

                  <div className="mypage-row">
                    <span>성별</span>
                    <div className="gender-buttons">
                      <button type="button" className={gender === "MALE" ? "gender-button is-selected" : "gender-button"} onClick={() => handleGenderClick("MALE")}>남</button>
                      <button type="button" className={gender === "FEMALE" ? "gender-button is-selected" : "gender-button"} onClick={() => handleGenderClick("FEMALE")}>여</button>
                      <button type="button" className={gender === "OTHER" ? "gender-button is-selected" : "gender-button"} onClick={() => handleGenderClick("OTHER")}>기타</button>
                    </div>
                  </div>

                  <label className="mypage-row">
                    <span>나이</span>
                    <input value={age} onChange={(e) => setAge(e.target.value.replace(/[^0-9]/g, ""))} type="text" placeholder="선택 입력" />
                  </label>
                </div>

                <div className="mypage-actions mypage-actions-right">
                  <button className="secondary-button" type="button" onClick={handleCancelProfile}>취소</button>
                  <button className={`mypage-submit-button ${isProfileValid ? "is-active" : ""}`} disabled={!isProfileValid} type="button" onClick={handleProfileSubmit}>변경 내용 저장</button>
                </div>
              </div>
            ) : (
              <div className="mypage-section">
                <div className="mypage-head"><h1>비밀번호 변경</h1></div>

                <div className="mypage-form-grid">
                  <label className="mypage-row">
                    <span>현재 비밀번호</span>
                    <div className="password-field-box">
                      <div className="mypage-password-input-wrap">
                        <input
                          value={currentPassword}
                          onChange={(e) => setCurrentPassword(e.target.value)}
                          type={showCurrentPassword ? "text" : "password"}
                          placeholder="현재 비밀번호를 입력해 주세요"
                        />
                        <button
                          type="button"
                          className="mypage-eye-button"
                          onClick={() => setShowCurrentPassword((prev) => !prev)}
                          aria-label={showCurrentPassword ? "현재 비밀번호 숨기기" : "현재 비밀번호 보기"}
                        >
                          {showCurrentPassword ? <EyeOffIcon /> : <EyeIcon />}
                        </button>
                      </div>
                      <p className="password-reset-text">비밀번호를 설정하지 않았거나 잊으셨나요? <button type="button" className="password-reset-link" onClick={() => onNavigate("password-reset")}>비밀번호 재설정</button></p>
                    </div>
                  </label>

                  <label className="mypage-row">
                    <span>새 비밀번호</span>
                    <div className="password-field-box">
                      <div className="mypage-password-input-wrap">
                        <input
                          value={nextPassword}
                          onChange={(e) => setNextPassword(e.target.value)}
                          type={showNextPassword ? "text" : "password"}
                          placeholder="새 비밀번호를 입력해 주세요"
                        />
                        <button
                          type="button"
                          className="mypage-eye-button"
                          onClick={() => setShowNextPassword((prev) => !prev)}
                          aria-label={showNextPassword ? "새 비밀번호 숨기기" : "새 비밀번호 보기"}
                        >
                          {showNextPassword ? <EyeOffIcon /> : <EyeIcon />}
                        </button>
                      </div>
                      <PasswordRules password={nextPassword} />
                    </div>
                  </label>

                  <label className="mypage-row">
                    <span>새 비밀번호 확인</span>
                    <div className="mypage-password-input-wrap">
                      <input
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        type={showConfirmPassword ? "text" : "password"}
                        placeholder="새 비밀번호를 다시 입력해 주세요"
                      />
                      <button
                        type="button"
                        className="mypage-eye-button"
                        onClick={() => setShowConfirmPassword((prev) => !prev)}
                        aria-label={showConfirmPassword ? "새 비밀번호 확인 숨기기" : "새 비밀번호 확인 보기"}
                      >
                        {showConfirmPassword ? <EyeOffIcon /> : <EyeIcon />}
                      </button>
                    </div>
                  </label>
                </div>

                <div className="mypage-actions mypage-actions-right">
                  <button className="secondary-button" type="button" onClick={() => { setCurrentPassword(""); setNextPassword(""); setConfirmPassword(""); }}>취소</button>
                  <button className={`mypage-submit-button ${isPasswordValid ? "is-active" : ""}`} disabled={!isPasswordValid} type="button" onClick={handlePasswordSubmit}>변경 내용 저장</button>
                </div>
              </div>
            )}
          </section>
        </div>
      </main>

      {isEmailVerifyModalOpen && (
        <div className="email-verify-overlay">
          <div className="email-verify-modal">
            <button type="button" className="email-verify-close" onClick={() => setIsEmailVerifyModalOpen(false)}>×</button>
            <h3>이메일 인증</h3>
            <p className="email-verify-address"><strong>{email}</strong></p>
            <div className="email-verify-code-row">
              <label>인증코드</label>
              <input type="text" value={emailVerifyCode} onChange={(event) => setEmailVerifyCode(event.target.value.replace(/\s/g, ""))} placeholder="인증코드 8자리를 입력하세요" maxLength={8} disabled={emailVerifyTimeLeft <= 0 || isEmailVerified} />
              <span>{formatEmailVerifyTime(emailVerifyTimeLeft)}</span>
            </div>
            <div className="email-verify-button-row">
              <button type="button" disabled={emailVerifyCode.length !== 8 || emailVerifyTimeLeft <= 0 || isEmailVerified} onClick={handleEmailChangeCodeVerify}>확인</button>
              <button type="button" className="email-verify-resend-button" disabled={emailVerifyTimeLeft > 0} onClick={handleEmailChangeCodeResend}>인증코드 재발송</button>
            </div>
            {isEmailVerified && <p className="email-verify-success">이메일 인증이 완료되었습니다.</p>}
          </div>
        </div>
      )}

      <footer className="footer">
        <button type="button" onClick={() => onNavigate("guide")}>서비스 소개</button>
        <button type="button" onClick={() => onNavigate("terms")}>이용약관</button>
        <button type="button" onClick={() => onNavigate("privacy")}>개인정보 처리방침</button>
        <button type="button" onClick={() => onNavigate("security-contact")}>보안 문의</button>
      </footer>
    </div>
  );
}

export default MyPage;
