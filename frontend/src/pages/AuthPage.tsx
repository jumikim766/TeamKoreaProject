import { useMemo, useState } from "react";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import "../styles/Dashboard.css";

type AuthPagesProps = {
  mode: "login" | "signup";
  theme: "light" | "dark";
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
};

function AuthPages({
  mode,
  theme,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
}: AuthPagesProps) {
  const isLogin = mode === "login";

  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [showLoginPassword, setShowLoginPassword] = useState(false);
  const [loginEmailTouched, setLoginEmailTouched] = useState(false);
  const [loginError, setLoginError] = useState("");

  const [signupName, setSignupName] = useState("");
  const [signupId, setSignupId] = useState("");
  const [signupEmail, setSignupEmail] = useState("");
  const [signupPassword, setSignupPassword] = useState("");
  const [signupPasswordConfirm, setSignupPasswordConfirm] = useState("");

  const [showSignupPassword, setShowSignupPassword] = useState(false);
  const [showSignupPasswordConfirm, setShowSignupPasswordConfirm] =
    useState(false);

  const [idCheckMessage, setIdCheckMessage] = useState("");
  const [emailCheckMessage, setEmailCheckMessage] = useState("");
  const [isIdAvailable, setIsIdAvailable] = useState(false);
  const [isEmailAvailable, setIsEmailAvailable] = useState(false);

  const passwordRules = useMemo(
    () => [
      {
        label: "영문 대/소문자 포함",
        valid: /(?=.*[a-z])(?=.*[A-Z])/.test(signupPassword),
      },
      {
        label: "숫자 포함",
        valid: /[0-9]/.test(signupPassword),
      },
      {
        label: "특수문자 포함",
        valid: /[!@#$%^&*(),.?":{}|<>_\-\\[\];'/+=`~]/.test(signupPassword),
      },
      {
        label: "8자 이상",
        valid: signupPassword.length >= 8,
      },
    ],
    [signupPassword]
  );

  const isPasswordValid = passwordRules.every((rule) => rule.valid);
  const isPasswordSame =
    signupPasswordConfirm.length > 0 &&
    signupPassword === signupPasswordConfirm;

  const canLogin = loginEmail.trim() !== "" && loginPassword.trim() !== "";

  const canSignup =
    signupName.trim() !== "" &&
    signupId.trim() !== "" &&
    signupEmail.trim() !== "" &&
    isIdAvailable &&
    isEmailAvailable &&
    isPasswordValid &&
    isPasswordSame;

  const handleLoginSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    setLoginEmailTouched(true);

    if (!loginEmail.trim()) {
      setLoginError("");
      return;
    }

    if (!loginPassword.trim()) {
      setLoginError("");
      return;
    }

    // 실제 API 연결 전 임시 실패 문구
    setLoginError("이메일 또는 비밀번호를 잘못 입력하셨습니다.");
  };

  const handleCheckId = () => {
    if (!signupId.trim()) {
      setIsIdAvailable(false);
      setIdCheckMessage("아이디를 입력해주세요.");
      return;
    }

    // 백엔드 API 연결 전 임시 중복 예시
    if (signupId.trim().toLowerCase() === "minseo") {
      setIsIdAvailable(false);
      setIdCheckMessage("이미 사용 중인 아이디입니다.");
      return;
    }

    setIsIdAvailable(true);
    setIdCheckMessage("사용 가능한 아이디입니다.");
  };

  const handleCheckEmail = () => {
    if (!signupEmail.trim()) {
      setIsEmailAvailable(false);
      setEmailCheckMessage("이메일을 입력해주세요.");
      return;
    }

    // 백엔드 API 연결 전 임시 중복 예시
    if (signupEmail.trim().toLowerCase() === "minseo@example.com") {
      setIsEmailAvailable(false);
      setEmailCheckMessage("이미 사용 중인 이메일입니다.");
      return;
    }

    setIsEmailAvailable(true);
    setEmailCheckMessage("사용 가능한 이메일입니다.");
  };

  const handleSignupSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!canSignup) return;

    console.log("회원가입 정보", {
      name: signupName,
      id: signupId,
      email: signupEmail,
      password: signupPassword,
    });
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
  onGoMyPage={onGoHome}
  onLogout={() => {}}
/>
      <Navbar onNavigate={onGoHome} />

      <main className="auth-main auth-main-centered">
        <section className="auth-layout auth-layout-centered">
          <div className="auth-card auth-card-centered">
            <div className="auth-card-head">
              <p className="eyebrow">
                {isLogin ? "WELCOME BACK" : "JOIN URL GUARD"}
              </p>
              <h2>{isLogin ? "로그인" : "회원가입"}</h2>
              <p>
                {isLogin
                  ? "이메일과 비밀번호를 입력해 로그인할 수 있습니다."
                  : "이름, 아이디, 이메일, 비밀번호를 입력해 회원가입할 수 있습니다."}
              </p>
            </div>

      <div className="social-button-group">
  <button
    type="button"
    className="google-button"
    onClick={() => {
      window.location.href =
        "http://localhost:8080/oauth2/authorization/google";
    }}
  >
    <span className="google-mark">G</span>
    Google로 {isLogin ? "로그인" : "회원가입"} 계속하기
  </button>

  <button
    type="button"
    className="naver-button"
    onClick={() => {
      window.location.href =
        "http://localhost:8080/oauth2/authorization/naver";
    }}
  >
    <span className="naver-mark">N</span>
    naver로 {isLogin ? "로그인" : "회원가입"} 계속하기
  </button>
</div>

            <div className="auth-divider">
              <span>또는 이메일로 계속</span>
            </div>

            {isLogin ? (
              <form className="auth-form" onSubmit={handleLoginSubmit}>
                <label className="auth-field">
                  <span>이메일</span>
                  <input
                    type="email"
                    value={loginEmail}
                    onChange={(event) => setLoginEmail(event.target.value)}
                    onBlur={() => setLoginEmailTouched(true)}
                  />
                  {loginEmailTouched && !loginEmail.trim() && (
                    <p className="field-error">
                      이메일을 입력하지 않았습니다.
                    </p>
                  )}
                </label>

                <label className="auth-field">
                  <span>비밀번호</span>
                  <div className="password-field">
                    <input
                      type={showLoginPassword ? "text" : "password"}
                      value={loginPassword}
                      onChange={(event) =>
                        setLoginPassword(event.target.value)
                      }
                    />
                    <button
                      type="button"
                      className="password-eye-button"
                      onClick={() =>
                        setShowLoginPassword((prevValue) => !prevValue)
                      }
                    >
                      {showLoginPassword ? "🙈" : "👁"}
                    </button>
                  </div>
                </label>

                <div className="auth-inline-actions">
                  <button type="button" className="auth-text-button">
                    비밀번호 찾기
                  </button>
                </div>

                {loginError && <p className="auth-error-message">{loginError}</p>}

                <button
                  type="submit"
                  className="primary-button auth-submit"
                  disabled={!canLogin}
                >
                  로그인
                </button>

                <div className="auth-footer">
                  <span>아직 계정이 없나요?</span>
                  <button
                    type="button"
                    className="auth-switch"
                    onClick={onGoSignup}
                  >
                    회원가입
                  </button>
                </div>
              </form>
            ) : (
              <form className="auth-form" onSubmit={handleSignupSubmit}>
                <label className="auth-field">
                  <span>이름</span>
                  <input
                    type="text"
                    value={signupName}
                    onChange={(event) => setSignupName(event.target.value)}
                  />
                </label>

                <label className="auth-field">
                  <span>아이디</span>
                  <div className="auth-check-row">
                    <input
                      type="text"
                      value={signupId}
                      onChange={(event) => {
                        setSignupId(event.target.value);
                        setIsIdAvailable(false);
                        setIdCheckMessage("");
                      }}
                    />
                    <button
                      type="button"
                      className="check-button"
                      onClick={handleCheckId}
                    >
                      중복 확인
                    </button>
                  </div>
                  {idCheckMessage && (
                    <p
                      className={
                        isIdAvailable ? "field-success" : "field-error"
                      }
                    >
                      {idCheckMessage}
                    </p>
                  )}
                </label>

                <label className="auth-field">
                  <span>이메일</span>
                  <div className="auth-check-row">
                    <input
                      type="email"
                      value={signupEmail}
                      onChange={(event) => {
                        setSignupEmail(event.target.value);
                        setIsEmailAvailable(false);
                        setEmailCheckMessage("");
                      }}
                    />
                    <button
                      type="button"
                      className="check-button"
                      onClick={handleCheckEmail}
                    >
                      중복 확인
                    </button>
                  </div>
                  {emailCheckMessage && (
                    <p
                      className={
                        isEmailAvailable ? "field-success" : "field-error"
                      }
                    >
                      {emailCheckMessage}
                    </p>
                  )}
                </label>

                <label className="auth-field">
                  <span>비밀번호</span>
                  <div className="password-field">
                    <input
                      type={showSignupPassword ? "text" : "password"}
                      value={signupPassword}
                      onChange={(event) =>
                        setSignupPassword(event.target.value)
                      }
                    />
                    <button
                      type="button"
                      className="password-eye-button"
                      onClick={() =>
                        setShowSignupPassword((prevValue) => !prevValue)
                      }
                    >
                      {showSignupPassword ? "🙈" : "👁"}
                    </button>
                  </div>

                  <ul className="password-rule-list">
                    {passwordRules.map((rule) => (
                      <li
                        key={rule.label}
                        className={rule.valid ? "is-valid" : ""}
                      >
                        {rule.label}
                      </li>
                    ))}
                  </ul>
                </label>

                <label className="auth-field">
                  <span>비밀번호 확인</span>
                  <div className="password-field">
                    <input
                      type={showSignupPasswordConfirm ? "text" : "password"}
                      value={signupPasswordConfirm}
                      onChange={(event) =>
                        setSignupPasswordConfirm(event.target.value)
                      }
                    />
                    <button
                      type="button"
                      className="password-eye-button"
                      onClick={() =>
                        setShowSignupPasswordConfirm((prevValue) => !prevValue)
                      }
                    >
                      {showSignupPasswordConfirm ? "🙈" : "👁"}
                    </button>
                  </div>

                  {signupPasswordConfirm.length > 0 && !isPasswordSame && (
                    <p className="field-error">비밀번호가 다릅니다.</p>
                  )}

                  {signupPasswordConfirm.length > 0 && isPasswordSame && (
                    <p className="field-success">비밀번호가 일치합니다.</p>
                  )}
                </label>

                <button
                  type="submit"
                  className="primary-button auth-submit"
                  disabled={!canSignup}
                >
                  회원가입
                </button>

                <div className="auth-footer">
                  <span>이미 계정이 있나요?</span>
                  <button
                    type="button"
                    className="auth-switch"
                    onClick={onGoLogin}
                  >
                    로그인
                  </button>
                </div>
              </form>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}

export default AuthPages;