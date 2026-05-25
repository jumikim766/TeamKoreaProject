import { useEffect, useMemo, useState } from "react";
import type { FormEvent } from "react";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import "../styles/AuthPage.css";

import {
  checkUsername,
  goSocialLogin,
  login,
  signup,
} from "../api/authApi";
import { saveAccessToken } from "../utils/token";
import { getErrorMessage } from "../api/errorMessage";
import "../styles/Dashboard.css";

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

type AuthPagesProps = {
  mode: "login" | "signup";
  theme: "light" | "dark";
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
  onNavigate: (view: PageViewTarget) => void;
  onLoginSuccess: (name?: string) => void;
};

function EyeIcon() {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

function EyeOffIcon() {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49" />
      <path d="M14.084 14.158a3 3 0 0 1-4.242-4.242" />
      <path d="M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143" />
      <path d="m2 2 20 20" />
    </svg>
  );
}

function AuthPages({
  mode,
  theme,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
  onLoginSuccess,
}: AuthPagesProps) {
  const isLogin = mode === "login";

  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [showLoginPassword, setShowLoginPassword] = useState(false);
  const [loginError, setLoginError] = useState("");

  const [signupName, setSignupName] = useState("");
  const [signupId, setSignupId] = useState("");
  const [signupEmail, setSignupEmail] = useState("");
  const [signupPhone, setSignupPhone] = useState("");
  const [signupPassword, setSignupPassword] = useState("");
  const [signupPasswordConfirm, setSignupPasswordConfirm] = useState("");
  const [showSignupPassword, setShowSignupPassword] = useState(false);
  const [showSignupPasswordConfirm, setShowSignupPasswordConfirm] =
    useState(false);

  const [idCheckMessage, setIdCheckMessage] = useState("");
  const [emailCheckMessage, setEmailCheckMessage] = useState("");
  const [isIdAvailable, setIsIdAvailable] = useState(false);
  const [isEmailAvailable, setIsEmailAvailable] = useState(false);

  const [isEmailVerifyModalOpen, setIsEmailVerifyModalOpen] = useState(false);
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [emailVerifyCode, setEmailVerifyCode] = useState("");
  const [emailVerifyTimeLeft, setEmailVerifyTimeLeft] = useState(180);

  const passwordRules = useMemo(
    () => [
      {
        label: "영문 대/소문자 포함",
        valid: /(?=.*[a-z])(?=.*[A-Z])/.test(signupPassword),
      },
      { label: "숫자 포함", valid: /[0-9]/.test(signupPassword) },
      { label: "특수문자 포함", valid: /[^A-Za-z0-9]/.test(signupPassword) },
      { label: "8자 이상", valid: signupPassword.length >= 8 },
    ],
    [signupPassword]
  );

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

  const isPasswordValid =
    passwordRules.every((rule) => rule.valid) &&
    signupPassword.length >= 8 &&
    signupPassword.length <= 20;

  const isPasswordSame =
    signupPasswordConfirm.length > 0 &&
    signupPassword === signupPasswordConfirm;

  const onlyPhoneNumber = signupPhone.replace(/-/g, "");
  const isPhoneValid = /^010\d{8}$/.test(onlyPhoneNumber);

  const canLogin = loginEmail.trim() !== "" && loginPassword.trim() !== "";

  const isSignupFilled =
    signupName.trim() !== "" &&
    signupId.trim() !== "" &&
    signupEmail.trim() !== "" &&
    signupPhone.trim() !== "" &&
    signupPassword.trim() !== "" &&
    signupPasswordConfirm.trim() !== "";

  const handleLoginSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canLogin) return;

    try {
      const res = await login(loginEmail.trim(), loginPassword);
      const data = res.data.data;

      if (!data?.accessToken) {
        setLoginError("로그인 응답이 올바르지 않습니다.");
        return;
      }

      saveAccessToken(data.accessToken);
      localStorage.setItem("userName", data.user?.name ?? "");
      setLoginError("");
      onLoginSuccess(data.user?.name);
    } catch (error) {
      setLoginError(getErrorMessage(error, "로그인에 실패했습니다."));
    }
  };

  const handleCheckId = async () => {
    const username = signupId.trim();

    if (!/^[A-Za-z0-9_]{4,20}$/.test(username)) {
      setIsIdAvailable(false);
      setIdCheckMessage("아이디는 영문, 숫자, _ 포함 4~20자로 입력해주세요.");
      return;
    }

    try {
      const res = await checkUsername(username);
      const available = Boolean(res.data.data?.available);
      setIsIdAvailable(available);
      setIdCheckMessage(
        available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다."
      );
    } catch (error) {
      setIsIdAvailable(false);
      setIdCheckMessage(
        getErrorMessage(error, "아이디 중복 확인에 실패했습니다.")
      );
    }
  };

  const handleCheckEmail = async () => {
    const email = signupEmail.trim();

    if (!email) {
      setIsEmailAvailable(false);
      setEmailCheckMessage("이메일을 입력해주세요.");
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setIsEmailAvailable(false);
      setEmailCheckMessage("올바른 이메일 형식으로 입력해주세요.");
      return;
    }

    setIsEmailAvailable(true);
    setIsEmailVerified(false);
    setEmailVerifyCode("");
    setEmailVerifyTimeLeft(180);
    setEmailCheckMessage("인증코드가 발송되었습니다.");
    setIsEmailVerifyModalOpen(true);
  };

  const handleResendEmailCode = () => {
    setIsEmailVerified(false);
    setEmailVerifyCode("");
    setEmailVerifyTimeLeft(180);
    setEmailCheckMessage("인증코드가 재발송되었습니다.");
  };

  const handleSignupSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!isSignupFilled) return;

    if (!/^[A-Za-z0-9_]{4,20}$/.test(signupId.trim())) {
      alert("아이디는 4~20자, 영문/숫자/_ 만 사용할 수 있습니다.");
      return;
    }

    if (signupName.length > 30 || /\s/.test(signupName)) {
      alert("이름은 최대 30자이며 공백을 사용할 수 없습니다.");
      return;
    }

    if (!isPhoneValid) {
      alert("전화번호는 010으로 시작하는 총 11자리 숫자여야 합니다.");
      return;
    }

    if (!isIdAvailable) {
      alert("아이디 중복 확인을 완료해주세요.");
      return;
    }

    if (!isEmailAvailable) {
      alert("이메일 인증을 완료해주세요.");
      return;
    }

    if (!isPasswordValid) {
      alert("비밀번호 조건을 모두 만족해주세요.");
      return;
    }

    if (!isPasswordSame) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    try {
      await signup({
        username: signupId.trim(),
        email: signupEmail.trim(),
        password: signupPassword,
        name: signupName.replace(/\s/g, ""),
        phone: signupPhone.replace(/-/g, ""),
      });

      alert("회원가입이 완료되었습니다. 로그인해주세요.");
      onGoLogin();
    } catch (error) {
      alert(getErrorMessage(error, "회원가입에 실패했습니다."));
    }
  };

  return (
    <div className="dashboard-shell">
      <Header
        theme={theme}
        currentView={mode}
        isLoggedIn={false}
        onToggleTheme={onToggleTheme}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onGoMyPage={onGoMyPage}
        onLogout={() => {}}
      />

      <Navbar onNavigate={onNavigate} />

      <main className="login-page-main">
        <section className="login-card">
          <div className="login-social-area">
            <button
              type="button"
              className="login-social-button"
              onClick={() => goSocialLogin("google")}
            >
              <span className="google-icon">G</span>
              Google로 {isLogin ? "로그인" : "회원가입"}
            </button>

            <button
              type="button"
              className="login-social-button"
              onClick={() => goSocialLogin("naver")}
            >
              <span className="naver-icon">N</span>
              Naver로 {isLogin ? "로그인" : "회원가입"}
            </button>
          </div>

          <div className="login-divider">
            <span>또는 이메일로 계속</span>
          </div>

          {isLogin ? (
            <form className="login-form" onSubmit={handleLoginSubmit}>
              <label className="login-field">
                <span>이메일</span>
                <input
                  type="email"
                  value={loginEmail}
                  onChange={(event) => {
                    setLoginEmail(event.target.value);
                    setLoginError("");
                  }}
                  placeholder="jumi@example.com"
                />
              </label>

              <label className="login-field">
                <span>비밀번호</span>
                <div className="login-password-wrap">
                  <input
                    type={showLoginPassword ? "text" : "password"}
                    value={loginPassword}
                    onChange={(event) => {
                      setLoginPassword(event.target.value);
                      setLoginError("");
                    }}
                    placeholder="비밀번호를 입력해주세요"
                  />
                  <button
                    type="button"
                    className="login-eye-button"
                    onClick={() => setShowLoginPassword((prev) => !prev)}
                  >
                    {showLoginPassword ? <EyeOffIcon /> : <EyeIcon />}
                  </button>
                </div>
              </label>

              <button type="button" className="find-password-button">
                비밀번호 찾기
              </button>

              {loginError && <p className="login-error-text">{loginError}</p>}

              <button
                type="submit"
                className={`login-submit-button ${canLogin ? "active" : ""}`}
                disabled={!canLogin}
              >
                로그인
              </button>

              <div className="login-bottom-text">
                <span>아직 계정이 없나요?</span>
                <button type="button" onClick={onGoSignup}>
                  회원가입
                </button>
              </div>
            </form>
          ) : (
            <form className="login-form" onSubmit={handleSignupSubmit}>
              <label className="login-field">
                <span>이름</span>
                <input
                  type="text"
                  value={signupName}
                  onChange={(event) =>
                    setSignupName(event.target.value.replace(/\s/g, ""))
                  }
                  placeholder="이름을 입력해주세요"
                  maxLength={30}
                />
              </label>

              <label className="login-field">
                <span>아이디</span>
                <div className="login-check-row">
                  <input
                    type="text"
                    value={signupId}
                    onChange={(event) => {
                      setSignupId(event.target.value.replace(/\s/g, ""));
                      setIsIdAvailable(false);
                      setIdCheckMessage("");
                    }}
                    placeholder="아이디를 입력해주세요"
                    maxLength={20}
                  />
                  <button type="button" onClick={handleCheckId}>
                    중복 확인
                  </button>
                </div>
                {idCheckMessage && (
                  <p
                    className={
                      isIdAvailable ? "login-success-text" : "login-error-text"
                    }
                  >
                    {idCheckMessage}
                  </p>
                )}
              </label>

              <label className="login-field">
                <span>이메일</span>
                <div className="login-check-row">
                  <input
                    type="email"
                    value={signupEmail}
                    onChange={(event) => {
                      setSignupEmail(event.target.value.replace(/\s/g, ""));
                      setIsEmailAvailable(false);
                      setEmailCheckMessage("");
                      setIsEmailVerified(false);
                      setEmailVerifyCode("");
                    }}
                    placeholder="이메일을 입력해주세요"
                  />
                  <button type="button" onClick={handleCheckEmail}>
                    인증코드 발송
                  </button>
                </div>

                {emailCheckMessage && (
                  <p
                    className={
                      isEmailAvailable ? "login-success-text" : "login-error-text"
                    }
                  >
                    {emailCheckMessage}
                  </p>
                )}
              </label>

              <label className="login-field">
                <span>전화번호</span>
                <input
                  type="tel"
                  value={signupPhone}
                  onChange={(event) =>
                    setSignupPhone(event.target.value.replace(/\s/g, ""))
                  }
                  placeholder="전화번호를 입력해주세요"
                  maxLength={13}
                />
              </label>

              <label className="login-field">
                <span>비밀번호</span>
                <div className="login-password-wrap">
                  <input
                    type={showSignupPassword ? "text" : "password"}
                    value={signupPassword}
                    onChange={(event) => setSignupPassword(event.target.value)}
                    placeholder="비밀번호를 입력해주세요"
                    maxLength={20}
                  />
                  <button
                    type="button"
                    className="login-eye-button"
                    onClick={() => setShowSignupPassword((prev) => !prev)}
                  >
                    {showSignupPassword ? <EyeOffIcon /> : <EyeIcon />}
                  </button>
                </div>

                <ul className="login-password-rules">
                  {passwordRules.map((rule) => (
                    <li key={rule.label} className={rule.valid ? "active" : ""}>
                      {rule.label}
                    </li>
                  ))}
                </ul>
              </label>

              <label className="login-field">
                <span>비밀번호 확인</span>
                <div className="login-password-wrap">
                  <input
                    type={showSignupPasswordConfirm ? "text" : "password"}
                    value={signupPasswordConfirm}
                    onChange={(event) =>
                      setSignupPasswordConfirm(event.target.value)
                    }
                    placeholder="비밀번호를 한 번 더 입력해주세요"
                    maxLength={20}
                  />
                  <button
                    type="button"
                    className="login-eye-button"
                    onClick={() =>
                      setShowSignupPasswordConfirm((prev) => !prev)
                    }
                  >
                    {showSignupPasswordConfirm ? <EyeOffIcon /> : <EyeIcon />}
                  </button>
                </div>

                {signupPasswordConfirm && (
                  <p
                    className={
                      isPasswordSame
                        ? "login-success-text"
                        : "login-error-text"
                    }
                  >
                    {isPasswordSame
                      ? "비밀번호가 일치합니다."
                      : "비밀번호가 다릅니다."}
                  </p>
                )}
              </label>

              <button
                type="submit"
                className={`login-submit-button ${
                  isSignupFilled ? "active" : ""
                }`}
                disabled={!isSignupFilled}
              >
                회원가입
              </button>

              <div className="login-bottom-text">
                <span>이미 계정이 있나요?</span>
                <button type="button" onClick={onGoLogin}>
                  로그인
                </button>
              </div>
            </form>
          )}

          {isEmailVerifyModalOpen && (
            <div className="email-verify-overlay">
              <div className="email-verify-modal">
                <button
                  type="button"
                  className="email-verify-close"
                  onClick={() => setIsEmailVerifyModalOpen(false)}
                >
                  ×
                </button>

                <h3>이메일 인증</h3>

                <p className="email-verify-address">
                  <strong>{signupEmail}</strong>
                </p>

                <div className="email-verify-code-row">
                  <label>인증코드</label>

                  <input
                    type="text"
                    value={emailVerifyCode}
                    onChange={(event) =>
                      setEmailVerifyCode(event.target.value.replace(/\D/g, ""))
                    }
                    placeholder="인증코드 8자리를 입력하세요"
                    maxLength={8}
                    disabled={emailVerifyTimeLeft <= 0 || isEmailVerified}
                  />

                  <span>{formatEmailVerifyTime(emailVerifyTimeLeft)}</span>
                </div>

                <div className="email-verify-button-row">
                  <button
                    type="button"
                    disabled={
                      emailVerifyCode.length !== 8 ||
                      emailVerifyTimeLeft <= 0 ||
                      isEmailVerified
                    }
                    onClick={() => {
  setIsEmailVerified(true);
  setIsEmailAvailable(true);
  setEmailCheckMessage("이메일 인증이 완료되었습니다.");

  setTimeout(() => {
    setIsEmailVerifyModalOpen(false);
  }, 800);
}}
                  >
                    확인
                  </button>

                  <button
                    type="button"
                    className="email-verify-resend-button"
                    disabled={emailVerifyTimeLeft > 0}
                    onClick={handleResendEmailCode}
                  >
                    인증코드 재발송
                  </button>
                </div>

                {isEmailVerified && (
                  <p className="email-verify-success">
                    이메일 인증이 완료되었습니다.
                  </p>
                )}
              </div>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default AuthPages;