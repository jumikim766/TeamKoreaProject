import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import type { ViewMode } from "../App";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import "../styles/AuthPage.css";
import "../styles/Dashboard.css";
import PasswordRules from "../components/auth/PasswordRules";

import {
  checkUsername,
  goSocialLogin,
  login,
  resetPassword,
  sendFindUsernameCode,
  sendPasswordResetCode,
  signup,
  sendSignupCode,
  verifyFindUsernameCode,
  verifySignupCode,
} from "../api/authApi";
import { saveAccessToken } from "../utils/token";
import { getErrorMessage } from "../api/errorMessage";

type AuthMode = "login" | "signup" | "find-username" | "password-reset";

type AuthPagesProps = {
  mode: AuthMode;
  theme: "light" | "dark";
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
  onGoNotifications?: () => void;
  unreadCount?: number;
  onNavigate: (view: ViewMode) => void;
  onLoginSuccess: (name?: string) => void;
};

function EyeIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

function EyeOffIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
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
  onGoNotifications,
  unreadCount = 0,
  onNavigate,
  onLoginSuccess,
}: AuthPagesProps) {
  const isLogin = mode === "login";
  const isSignup = mode === "signup";

  const [loginUsername, setLoginUsername] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [showLoginPassword, setShowLoginPassword] = useState(false);
  const [loginError, setLoginError] = useState("");

  const [signupName, setSignupName] = useState("");
  const [signupId, setSignupId] = useState("");
  const [signupEmail, setSignupEmail] = useState("");
  const [signupPassword, setSignupPassword] = useState("");
  const [signupPasswordConfirm, setSignupPasswordConfirm] = useState("");
  const [showSignupPassword, setShowSignupPassword] = useState(false);
  const [showSignupPasswordConfirm, setShowSignupPasswordConfirm] = useState(false);

  const [idCheckMessage, setIdCheckMessage] = useState("");
  const [emailCheckMessage, setEmailCheckMessage] = useState("");
  const [isIdAvailable, setIsIdAvailable] = useState(false);

  const [isEmailVerifyModalOpen, setIsEmailVerifyModalOpen] = useState(false);
  const [isEmailVerified, setIsEmailVerified] = useState(false);
  const [emailVerifyCode, setEmailVerifyCode] = useState("");
  const [emailVerifyTimeLeft, setEmailVerifyTimeLeft] = useState(180);

  const [findName, setFindName] = useState("");
  const [findEmail, setFindEmail] = useState("");
  const [findCode, setFindCode] = useState("");
  const [foundUsername, setFoundUsername] = useState("");
  const [findMessage, setFindMessage] = useState("");
  const [isFindCodeSent, setIsFindCodeSent] = useState(false);
  const [findCodeTimeLeft, setFindCodeTimeLeft] = useState(180);

  const [resetUsername, setResetUsername] = useState("");
  const [resetName, setResetName] = useState("");
  const [resetEmail, setResetEmail] = useState("");
  const [resetCode, setResetCode] = useState("");
  const [resetNewPassword, setResetNewPassword] = useState("");
  const [resetNewPasswordConfirm, setResetNewPasswordConfirm] = useState("");
  const [resetMessage, setResetMessage] = useState("");
  const [isResetCodeSent, setIsResetCodeSent] = useState(false);
  const [isResetCodeVerified, setIsResetCodeVerified] = useState(false);
  const [resetCodeTimeLeft, setResetCodeTimeLeft] = useState(180);
  const [showResetPassword, setShowResetPassword] = useState(false);
  const [showResetPasswordConfirm, setShowResetPasswordConfirm] = useState(false);

  const isValidPassword = (password: string) =>
    /(?=.*[a-z])(?=.*[A-Z])/.test(password) &&
    /[0-9]/.test(password) &&
    /[^A-Za-z0-9]/.test(password) &&
    password.length >= 8 &&
    password.length <= 20;

  useEffect(() => {
    if (!isEmailVerifyModalOpen || isEmailVerified) return;
    if (emailVerifyTimeLeft <= 0) return;

    const timer = window.setInterval(() => {
      setEmailVerifyTimeLeft((prev) => prev - 1);
    }, 1000);

    return () => window.clearInterval(timer);
  }, [isEmailVerifyModalOpen, isEmailVerified, emailVerifyTimeLeft]);

  useEffect(() => {
    if (!isFindCodeSent || foundUsername || findCodeTimeLeft <= 0) return;

    const timer = window.setInterval(() => {
      setFindCodeTimeLeft((prev) => prev - 1);
    }, 1000);

    return () => window.clearInterval(timer);
  }, [isFindCodeSent, foundUsername, findCodeTimeLeft]);

  useEffect(() => {
    if (!isResetCodeSent || isResetCodeVerified || resetCodeTimeLeft <= 0) return;

    const timer = window.setInterval(() => {
      setResetCodeTimeLeft((prev) => prev - 1);
    }, 1000);

    return () => window.clearInterval(timer);
  }, [isResetCodeSent, isResetCodeVerified, resetCodeTimeLeft]);

  const formatEmailVerifyTime = (seconds: number) => {
    const minute = Math.floor(seconds / 60);
    const second = seconds % 60;
    return `${minute}:${String(second).padStart(2, "0")}`;
  };

  const isPasswordValid = isValidPassword(signupPassword);

  const isPasswordSame =
    signupPasswordConfirm.length > 0 && signupPassword === signupPasswordConfirm;

  const canLogin = loginUsername.trim() !== "" && loginPassword.trim() !== "";

  const isSignupFilled =
    signupName.trim() !== "" &&
    signupId.trim() !== "" &&
    signupEmail.trim() !== "" &&
    signupPassword.trim() !== "" &&
    signupPasswordConfirm.trim() !== "";

  const isResetPasswordValid =
    isValidPassword(resetNewPassword) &&
    resetNewPassword === resetNewPasswordConfirm;

  const handleLoginSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!canLogin) return;

    try {
      const res = await login(loginUsername.trim(), loginPassword);
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
      setIdCheckMessage(available ? "사용 가능한 아이디입니다." : "이미 사용 중인 아이디입니다.");
    } catch (error) {
      setIsIdAvailable(false);
      setIdCheckMessage(getErrorMessage(error, "아이디 중복 확인에 실패했습니다."));
    }
  };

  const handleCheckEmail = async () => {
    const email = signupEmail.trim();

    if (!email) {
      setIsEmailVerified(false);
      setEmailCheckMessage("이메일을 입력해주세요.");
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setIsEmailVerified(false);
      setEmailCheckMessage("올바른 이메일 형식으로 입력해주세요.");
      return;
    }

    try {
      await sendSignupCode({ email });
      setIsEmailVerified(false);
      setEmailVerifyCode("");
      setEmailVerifyTimeLeft(180);
      setEmailCheckMessage("인증코드가 발송되었습니다.");
      setIsEmailVerifyModalOpen(true);
    } catch (error) {
      setIsEmailVerified(false);
      setEmailCheckMessage(getErrorMessage(error, "인증코드 발송에 실패했습니다."));
    }
  };

  const handleResendEmailCode = async () => {
    try {
      await sendSignupCode({ email: signupEmail.trim() });
      setIsEmailVerified(false);
      setEmailVerifyCode("");
      setEmailVerifyTimeLeft(180);
      setEmailCheckMessage("인증코드가 재발송되었습니다.");
    } catch (error) {
      setEmailCheckMessage(getErrorMessage(error, "인증코드 재발송에 실패했습니다."));
    }
  };

  const handleVerifyEmailCode = async () => {
    try {
      await verifySignupCode({ email: signupEmail.trim(), code: emailVerifyCode.trim() });
      setIsEmailVerified(true);
      setEmailCheckMessage("이메일 인증이 완료되었습니다.");
      alert("이메일 인증이 완료되었습니다.");
      setIsEmailVerifyModalOpen(false);
    } catch (error) {
      setIsEmailVerified(false);
      alert(getErrorMessage(error, "인증코드가 올바르지 않습니다."));
    }
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

    if (!isIdAvailable) {
      alert("아이디 중복 확인을 완료해주세요.");
      return;
    }

    if (!isEmailVerified) {
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
      });

      alert("회원가입이 완료되었습니다. 로그인해주세요.");
      onGoLogin();
    } catch (error) {
      alert(getErrorMessage(error, "회원가입에 실패했습니다."));
    }
  };

  const handleSendFindUsernameCode = async () => {
    if (!findName.trim() || !findEmail.trim()) {
      alert("이름과 이메일을 입력해주세요.");
      return;
    }

    try {
      await sendFindUsernameCode({ name: findName.trim(), email: findEmail.trim() });
      setIsFindCodeSent(true);
      setFindCode("");
      setFoundUsername("");
      setFindCodeTimeLeft(180);
      setFindMessage("인증코드가 이메일로 발송되었습니다.");
    } catch (error) {
      setFindMessage(getErrorMessage(error, "인증코드 발송에 실패했습니다."));
    }
  };

  const handleVerifyFindUsernameCode = async () => {
    if (!findCode.trim() || findCodeTimeLeft <= 0) return;

    try {
      const res = await verifyFindUsernameCode({ email: findEmail.trim(), code: findCode.trim() });
      const username = res.data.data?.username ?? "";
      setFoundUsername(username);
      setFindMessage(username ? "아이디를 찾았습니다." : "아이디 정보를 찾았습니다.");
    } catch (error) {
      setFindMessage(getErrorMessage(error, "인증코드 확인에 실패했습니다."));
    }
  };

  const handleSendPasswordResetCode = async () => {
    if (!resetName.trim() || !resetUsername.trim() || !resetEmail.trim()) {
      alert("이름, 아이디, 이메일을 모두 입력해주세요.");
      return;
    }

    try {
      await sendPasswordResetCode({ username: resetUsername.trim(), email: resetEmail.trim() });
      setIsResetCodeSent(true);
      setIsResetCodeVerified(false);
      setResetCode("");
      setResetNewPassword("");
      setResetNewPasswordConfirm("");
      setResetCodeTimeLeft(180);
      setResetMessage("인증코드가 이메일로 발송되었습니다.");
    } catch (error) {
      setResetMessage(getErrorMessage(error, "인증코드 발송에 실패했습니다."));
    }
  };

  const handleVerifyPasswordResetCode = () => {
    if (!resetCode.trim() || resetCodeTimeLeft <= 0) return;

    setIsResetCodeVerified(true);
    setResetMessage("인증코드 확인이 완료되었습니다. 새 비밀번호를 입력해주세요.");
  };

  const handlePasswordResetSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!isResetCodeVerified || !resetCode.trim() || !isResetPasswordValid) return;

    try {
      await resetPassword({
        username: resetUsername.trim(),
        email: resetEmail.trim(),
        code: resetCode.trim(),
        newPassword: resetNewPassword,
      });

      alert("비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해주세요.");
      onGoLogin();
    } catch (error) {
      setResetMessage(getErrorMessage(error, "비밀번호 재설정에 실패했습니다."));
    }
  };

  const renderLoginForm = () => (
    <form className="login-form" onSubmit={handleLoginSubmit}>
      <label className="login-field">
        <span>아이디</span>
        <input
          type="text"
          value={loginUsername}
          onChange={(event) => {
            setLoginUsername(event.target.value.replace(/\s/g, ""));
            setLoginError("");
          }}
          placeholder="아이디를 입력해주세요"
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
          <button type="button" className="login-eye-button" onClick={() => setShowLoginPassword((prev) => !prev)}>
            {showLoginPassword ? <EyeOffIcon /> : <EyeIcon />}
          </button>
        </div>
      </label>

      <div className="auth-find-links">
        <button type="button" onClick={() => onNavigate("find-username")}>아이디 찾기</button>
        <span>|</span>
        <button type="button" onClick={() => onNavigate("password-reset")}>비밀번호 찾기</button>
      </div>

      {loginError && <p className="login-error-text">{loginError}</p>}

      <button type="submit" className={`login-submit-button ${canLogin ? "active" : ""}`} disabled={!canLogin}>
        로그인
      </button>

      <div className="login-bottom-text">
        <span>아직 계정이 없나요?</span>
        <button type="button" onClick={onGoSignup}>회원가입</button>
      </div>
    </form>
  );

  const renderSignupForm = () => (
    <form className="login-form" onSubmit={handleSignupSubmit}>
      <label className="login-field">
        <span>이름</span>
        <input type="text" value={signupName} onChange={(event) => setSignupName(event.target.value.replace(/\s/g, ""))} placeholder="이름을 입력해주세요" maxLength={30} />
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
          <button type="button" onClick={handleCheckId}>중복 확인</button>
        </div>
        {idCheckMessage && <p className={isIdAvailable ? "login-success-text" : "login-error-text"}>{idCheckMessage}</p>}
      </label>

      <label className="login-field">
        <span>이메일</span>
        <div className="login-check-row">
          <input
            type="email"
            value={signupEmail}
            onChange={(event) => {
              setSignupEmail(event.target.value.replace(/\s/g, ""));
              setEmailCheckMessage("");
              setIsEmailVerified(false);
              setEmailVerifyCode("");
            }}
            placeholder="이메일을 입력해주세요"
          />
          <button type="button" onClick={handleCheckEmail}>인증코드 발송</button>
        </div>
        {emailCheckMessage && <p className={isEmailVerified ? "login-success-text" : "login-error-text"}>{emailCheckMessage}</p>}
      </label>

      <label className="login-field">
        <span>비밀번호</span>
        <div className="login-password-wrap">
          <input type={showSignupPassword ? "text" : "password"} value={signupPassword} onChange={(event) => setSignupPassword(event.target.value)} placeholder="비밀번호를 입력해주세요" maxLength={20} />
          <button type="button" className="login-eye-button" onClick={() => setShowSignupPassword((prev) => !prev)}>
            {showSignupPassword ? <EyeOffIcon /> : <EyeIcon />}
          </button>
        </div>
        <PasswordRules password={signupPassword} />
      </label>

      <label className="login-field">
        <span>비밀번호 확인</span>
        <div className="login-password-wrap">
          <input type={showSignupPasswordConfirm ? "text" : "password"} value={signupPasswordConfirm} onChange={(event) => setSignupPasswordConfirm(event.target.value)} placeholder="비밀번호를 한 번 더 입력해주세요" maxLength={20} />
          <button type="button" className="login-eye-button" onClick={() => setShowSignupPasswordConfirm((prev) => !prev)}>
            {showSignupPasswordConfirm ? <EyeOffIcon /> : <EyeIcon />}
          </button>
        </div>
        {signupPasswordConfirm && <p className={isPasswordSame ? "login-success-text" : "login-error-text"}>{isPasswordSame ? "비밀번호가 일치합니다." : "비밀번호가 다릅니다."}</p>}
      </label>

      <button type="submit" className={`login-submit-button ${isSignupFilled ? "active" : ""}`} disabled={!isSignupFilled}>회원가입</button>

      <div className="login-bottom-text">
        <span>이미 계정이 있나요?</span>
        <button type="button" onClick={onGoLogin}>로그인</button>
      </div>
    </form>
  );

  const renderFindUsernameForm = () => (
    <div className="login-form">
      <h2 className="auth-sub-title">아이디 찾기</h2>
      <label className="login-field">
        <span>이름</span>
        <input type="text" value={findName} onChange={(e) => setFindName(e.target.value.replace(/\s/g, ""))} placeholder="이름을 입력해주세요" />
      </label>
      <label className="login-field">
        <span>이메일</span>
        <div className="login-check-row">
          <input type="email" value={findEmail} onChange={(e) => setFindEmail(e.target.value.replace(/\s/g, ""))} placeholder="이메일을 입력해주세요" />
          <button type="button" onClick={handleSendFindUsernameCode}>인증코드 발송</button>
        </div>
      </label>
      {isFindCodeSent && (
        <label className="login-field">
          <span>인증코드</span>
          <div className="login-check-row auth-code-row">
            <input
              type="text"
              value={findCode}
              onChange={(e) => setFindCode(e.target.value.replace(/\s/g, ""))}
              placeholder="인증코드를 입력해주세요"
              maxLength={8}
              disabled={findCodeTimeLeft <= 0 || Boolean(foundUsername)}
            />
            <span className="auth-code-timer">{formatEmailVerifyTime(findCodeTimeLeft)}</span>
            <button
              type="button"
              onClick={handleVerifyFindUsernameCode}
              disabled={!findCode.trim() || findCodeTimeLeft <= 0 || Boolean(foundUsername)}
            >
              확인
            </button>
          </div>
        </label>
      )}
      {findMessage && <p className={foundUsername ? "login-success-text" : "login-error-text"}>{findMessage}</p>}
      {foundUsername && <div className="auth-result-box">찾은 아이디: <strong>{foundUsername}</strong></div>}
      <div className="login-bottom-text">
        <button type="button" onClick={onGoLogin}>로그인으로 돌아가기</button>
      </div>
    </div>
  );

  const renderPasswordResetForm = () => (
    <form className="login-form" onSubmit={handlePasswordResetSubmit}>
      <h2 className="auth-sub-title">비밀번호 찾기</h2>
      <label className="login-field">
        <span>이름</span>
        <input type="text" value={resetName} onChange={(e) => setResetName(e.target.value.replace(/\s/g, ""))} placeholder="이름을 입력해주세요" />
      </label>
      <label className="login-field">
        <span>아이디</span>
        <input type="text" value={resetUsername} onChange={(e) => setResetUsername(e.target.value.replace(/\s/g, ""))} placeholder="아이디를 입력해주세요" />
      </label>
      <label className="login-field">
        <span>이메일</span>
        <div className="login-check-row">
          <input type="email" value={resetEmail} onChange={(e) => setResetEmail(e.target.value.replace(/\s/g, ""))} placeholder="이메일을 입력해주세요" />
          <button type="button" onClick={handleSendPasswordResetCode}>인증코드 발송</button>
        </div>
      </label>
      {isResetCodeSent && (
        <label className="login-field">
          <span>인증코드</span>
          <div className="login-check-row auth-code-row">
            <input
              type="text"
              value={resetCode}
              onChange={(e) => {
                setResetCode(e.target.value.replace(/\s/g, ""));
                setIsResetCodeVerified(false);
                setResetNewPassword("");
                setResetNewPasswordConfirm("");
              }}
              placeholder="인증코드를 입력해주세요"
              maxLength={8}
              disabled={resetCodeTimeLeft <= 0 || isResetCodeVerified}
            />
            <span className="auth-code-timer">{formatEmailVerifyTime(resetCodeTimeLeft)}</span>
            <button
              type="button"
              onClick={handleVerifyPasswordResetCode}
              disabled={!resetCode.trim() || resetCodeTimeLeft <= 0 || isResetCodeVerified}
            >
              확인
            </button>
          </div>
        </label>
      )}
      {isResetCodeVerified && (
        <>
          <label className="login-field">
            <span>새 비밀번호</span>
            <div className="login-password-wrap">
              <input type={showResetPassword ? "text" : "password"} value={resetNewPassword} onChange={(e) => setResetNewPassword(e.target.value)} placeholder="새 비밀번호를 입력해주세요" maxLength={20} />
              <button type="button" className="login-eye-button" onClick={() => setShowResetPassword((prev) => !prev)}>{showResetPassword ? <EyeOffIcon /> : <EyeIcon />}</button>
            </div>
            <PasswordRules password={resetNewPassword} />
          </label>
          <label className="login-field">
            <span>새 비밀번호 확인</span>
            <div className="login-password-wrap">
              <input type={showResetPasswordConfirm ? "text" : "password"} value={resetNewPasswordConfirm} onChange={(e) => setResetNewPasswordConfirm(e.target.value)} placeholder="새 비밀번호를 한 번 더 입력해주세요" maxLength={20} />
              <button type="button" className="login-eye-button" onClick={() => setShowResetPasswordConfirm((prev) => !prev)}>{showResetPasswordConfirm ? <EyeOffIcon /> : <EyeIcon />}</button>
            </div>
            {resetNewPasswordConfirm && <p className={resetNewPassword === resetNewPasswordConfirm ? "login-success-text" : "login-error-text"}>{resetNewPassword === resetNewPasswordConfirm ? "비밀번호가 일치합니다." : "비밀번호가 다릅니다."}</p>}
          </label>
        </>
      )}
      {resetMessage && <p className={isResetCodeVerified ? "login-success-text" : "login-error-text"}>{resetMessage}</p>}
      <button type="submit" className={`login-submit-button ${isResetCodeVerified && isResetPasswordValid ? "active" : ""}`} disabled={!isResetCodeVerified || !isResetPasswordValid}>비밀번호 재설정</button>
      <div className="login-bottom-text">
        <button type="button" onClick={onGoLogin}>로그인으로 돌아가기</button>
      </div>
    </form>
  );

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
        onGoNotifications={onGoNotifications}
        unreadCount={unreadCount}
        onLogout={() => {}}
      />

      <Navbar currentView={mode} onNavigate={onNavigate} />

      <main className="login-page-main">
        <section className="login-card">
          {(isLogin || isSignup) && (
            <>
              <div className="login-social-area">
                <button type="button" className="login-social-button" onClick={() => goSocialLogin("google")}>
                  <span className="google-icon">G</span>
                  Google로 {isLogin ? "로그인" : "회원가입"}
                </button>

                <button type="button" className="login-social-button" onClick={() => goSocialLogin("naver")}>
                  <span className="naver-icon">N</span>
                  Naver로 {isLogin ? "로그인" : "회원가입"}
                </button>
              </div>

              <div className="login-divider">
                <span>또는 이메일로 계속</span>
              </div>
            </>
          )}

          {isLogin && renderLoginForm()}
          {isSignup && renderSignupForm()}
          {mode === "find-username" && renderFindUsernameForm()}
          {mode === "password-reset" && renderPasswordResetForm()}

          {isEmailVerifyModalOpen && (
            <div className="email-verify-overlay">
              <div className="email-verify-modal">
                <button type="button" className="email-verify-close" onClick={() => setIsEmailVerifyModalOpen(false)}>×</button>
                <h3>이메일 인증</h3>
                <p className="email-verify-address"><strong>{signupEmail}</strong></p>
                <div className="email-verify-code-row">
                  <label>인증코드</label>
                  <input
                    type="text"
                    value={emailVerifyCode}
                    onChange={(event) => setEmailVerifyCode(event.target.value.replace(/\s/g, ""))}
                    placeholder="인증코드 8자리를 입력하세요"
                    maxLength={8}
                    disabled={emailVerifyTimeLeft <= 0 || isEmailVerified}
                  />
                  <span>{formatEmailVerifyTime(emailVerifyTimeLeft)}</span>
                </div>
                <div className="email-verify-button-row">
                  <button type="button" disabled={emailVerifyCode.length !== 8 || emailVerifyTimeLeft <= 0 || isEmailVerified} onClick={handleVerifyEmailCode}>확인</button>
                  <button type="button" className="email-verify-resend-button" disabled={emailVerifyTimeLeft > 0} onClick={handleResendEmailCode}>인증코드 재발송</button>
                </div>
                {isEmailVerified && <p className="email-verify-success">이메일 인증이 완료되었습니다.</p>}
              </div>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}

export default AuthPages;