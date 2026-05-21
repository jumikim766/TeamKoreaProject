import { useEffect, useState } from "react";
import Header from "../components/Header";
import Navbar from "../components/Navbar";
import { checkEmail, checkUsername } from "../api/authApi";
import { changePassword, getMe, updateMe } from "../api/userApi";
import type { UserMe, UserUpdatePayload } from "../api/userApi";
import { getErrorMessage } from "../api/errorMessage";
import "../styles/MyPage.css";

type ThemeMode = "light" | "dark";

type MyPageTab = "profile" | "password";
type Gender = "" | "MALE" | "FEMALE";
type Provider = "LOCAL" | "GOOGLE" | "NAVER";

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
  onNavigate: (view: PageViewTarget) => void;
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

  const [isUsernameChecked, setIsUsernameChecked] = useState(false);
  const [isEmailChecked, setIsEmailChecked] = useState(false);

  const [currentPassword, setCurrentPassword] = useState("");
  const [nextPassword, setNextPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const isSocialUser = provider !== "LOCAL";

  useEffect(() => {
    const loadMyInfo = async () => {
      try {
        const res = await getMe();
        const user = res.data.data;

        if (!user) return;

        setOriginalUser(user);
        setName(user.name ?? "");
        setUsername(user.username ?? "");
        setEmail(user.email ?? "");
        setPhone(user.phoneMasked ?? "");
        setProvider(user.provider);
        setGender(user.gender === "MALE" || user.gender === "FEMALE" ? user.gender : "");
        setAge(user.age === null || user.age === undefined ? "" : String(user.age));

        setIsUsernameChecked(true);
        setIsEmailChecked(true);
      } catch (error) {
        alert(getErrorMessage(error, "회원 정보를 불러오지 못했습니다."));
      }
    };

    loadMyInfo();
  }, []);

  const phoneOnlyNumber = phone.replace(/-/g, "");

  const isNameValid = name.trim().length > 0;
  const isUsernameValid = /^[a-zA-Z0-9_]{4,20}$/.test(username);
  const isEmailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  const isPhoneMasked = phone.includes("*");
  const isPhoneValid =
    phone.trim() === "" || isPhoneMasked || /^010\d{8}$/.test(phoneOnlyNumber);
  const isAgeValid = age.trim() === "" || /^[0-9]+$/.test(age);

  const isProfileValid =
    isNameValid &&
    (isSocialUser || (isUsernameValid && isUsernameChecked)) &&
    (isSocialUser || (isEmailValid && isEmailChecked)) &&
    isPhoneValid &&
    isAgeValid;

  const hasPasswordLetter = /[a-zA-Z]/.test(nextPassword);
  const hasPasswordNumber = /[0-9]/.test(nextPassword);
  const hasPasswordSpecial = /[^a-zA-Z0-9]/.test(nextPassword);
  const hasPasswordLength = nextPassword.length >= 8;

  const isNextPasswordValid =
    hasPasswordLetter &&
    hasPasswordNumber &&
    hasPasswordSpecial &&
    hasPasswordLength;

  const isPasswordValid =
    currentPassword.trim().length >= 1 &&
    isNextPasswordValid &&
    confirmPassword.trim().length >= 1 &&
    nextPassword === confirmPassword;

  const handleUsernameCheck = async () => {
    if (!isUsernameValid) {
      alert("아이디는 4~20자, 영문/숫자/_ 만 사용할 수 있습니다.");
      return;
    }

    try {
      const res = await checkUsername(username.trim());
      const available = res.data.data?.available;

      if (available) {
        setIsUsernameChecked(true);
        alert("사용 가능한 아이디입니다.");
      } else {
        setIsUsernameChecked(false);
        alert("이미 사용 중인 아이디입니다.");
      }
    } catch (error) {
      alert(getErrorMessage(error, "아이디 중복 확인에 실패했습니다."));
    }
  };

  const handleEmailCheck = async () => {
    if (!isEmailValid) {
      alert("이메일 형식이 올바르지 않습니다.");
      return;
    }

    try {
      const res = await checkEmail(email.trim());
      const available = res.data.data?.available;

      if (available) {
        setIsEmailChecked(true);
        alert("사용 가능한 이메일입니다.");
      } else {
        setIsEmailChecked(false);
        alert("이미 사용 중인 이메일입니다.");
      }
    } catch (error) {
      alert(getErrorMessage(error, "이메일 중복 확인에 실패했습니다."));
    }
  };

  const handleProfileSubmit = async () => {
    if (!originalUser || !isProfileValid) return;

    const payload: UserUpdatePayload = {};

    if (name.trim() !== originalUser.name) {
      payload.name = name.trim();
    }

    if (!isSocialUser && username.trim() !== originalUser.username) {
      payload.username = username.trim();
    }

    if (!isSocialUser && email.trim() !== originalUser.email) {
      payload.email = email.trim();
    }

    if (phone.trim() !== "" && !isPhoneMasked && phoneOnlyNumber !== originalUser.phoneMasked) {
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
      const res = await updateMe(payload);
      const updatedUser = res.data.data;

      alert("회원 정보가 수정되었습니다.");

      if (updatedUser) {
        setOriginalUser(updatedUser);
        setName(updatedUser.name ?? "");
        setUsername(updatedUser.username ?? "");
        setEmail(updatedUser.email ?? "");
        setPhone(updatedUser.phoneMasked ?? "");
        setProvider(updatedUser.provider);
        setGender(
          updatedUser.gender === "MALE" || updatedUser.gender === "FEMALE"
            ? updatedUser.gender
            : ""
        );
        setAge(
          updatedUser.age === null || updatedUser.age === undefined
            ? ""
            : String(updatedUser.age)
        );
        setIsUsernameChecked(true);
        setIsEmailChecked(true);
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
              <div className="page-side-title">마이페이지</div>

              <button
                className={
                  tab === "profile"
                    ? "side-menu-button is-active"
                    : "side-menu-button"
                }
                onClick={() => setTab("profile")}
                type="button"
              >
                회원 정보 수정
              </button>

              {!isSocialUser && (
                <button
                  className={
                    tab === "password"
                      ? "side-menu-button is-active"
                      : "side-menu-button"
                  }
                  onClick={() => setTab("password")}
                  type="button"
                >
                  비밀번호 변경
                </button>
              )}
            </div>
          </aside>

          <section className="page-content-card">
            {tab === "profile" || isSocialUser ? (
              <div className="mypage-section">
                <div className="mypage-head">
                  <h1>회원 정보 수정</h1>
                </div>

                <div className="mypage-form-grid">
                  <label className="mypage-row">
                    <span>이름</span>
                    <input
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      type="text"
                      placeholder="이름을 입력하세요"
                    />
                  </label>

                  <label className="mypage-row">
                    <span>아이디</span>

                    <div className="mypage-inline-field">
                      <input
                        value={username}
                        onChange={(e) => {
                          setUsername(e.target.value);
                          setIsUsernameChecked(false);
                        }}
                        type="text"
                        placeholder="아이디를 입력하세요"
                        disabled={isSocialUser}
                      />

                      {!isSocialUser && (
                        <button
                          type="button"
                          className="mypage-check-button"
                          onClick={handleUsernameCheck}
                        >
                          중복확인
                        </button>
                      )}
                    </div>
                  </label>

                  <label className="mypage-row">
                    <span>전화번호</span>
                    <input
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      type="text"
                      placeholder="01012345678"
                    />
                  </label>

                  <label className="mypage-row">
                    <span>
                      이메일 <b>*</b>
                    </span>

                    <div className="mypage-inline-field">
                      <input
                        value={email}
                        onChange={(e) => {
                          setEmail(e.target.value);
                          setIsEmailChecked(false);
                        }}
                        type="email"
                        placeholder="example@email.com"
                        disabled={isSocialUser}
                      />

                      {!isSocialUser && (
                        <button
                          type="button"
                          className="mypage-check-button"
                          onClick={handleEmailCheck}
                        >
                          중복확인
                        </button>
                      )}
                    </div>
                  </label>

                  <div className="mypage-row">
                    <span>성별</span>

                    <div className="gender-buttons">
                      <button
                        type="button"
                        className={
                          gender === "MALE"
                            ? "gender-button is-selected"
                            : "gender-button"
                        }
                        onClick={() => setGender("MALE")}
                      >
                        남
                      </button>

                      <button
                        type="button"
                        className={
                          gender === "FEMALE"
                            ? "gender-button is-selected"
                            : "gender-button"
                        }
                        onClick={() => setGender("FEMALE")}
                      >
                        여
                      </button>
                    </div>
                  </div>

                  <label className="mypage-row">
                    <span>나이</span>
                    <input
                      value={age}
                      onChange={(e) => setAge(e.target.value)}
                      type="text"
                      placeholder="나이를 입력하세요"
                    />
                  </label>
                </div>

                <div className="mypage-actions mypage-actions-right">
                  <button className="secondary-button" type="button">
                    취소
                  </button>

                  <button
                    className={`mypage-submit-button ${
                      isProfileValid ? "is-active" : ""
                    }`}
                    disabled={!isProfileValid}
                    type="button"
                    onClick={handleProfileSubmit}
                  >
                    변경 내용 저장
                  </button>
                </div>
              </div>
            ) : (
              <div className="mypage-section">
                <div className="mypage-head">
                  <h1>비밀번호 변경</h1>
                </div>

                <div className="mypage-form-grid">
                  <label className="mypage-row">
                    <span>현재 비밀번호</span>

                    <div className="password-field-box">
                      <input
                        value={currentPassword}
                        onChange={(e) => setCurrentPassword(e.target.value)}
                        type="password"
                        placeholder="현재 비밀번호를 입력해 주세요"
                      />

                      <p className="password-reset-text">
                        비밀번호를 설정하지 않았거나 잊으셨나요?{" "}
                        <button
                          type="button"
                          className="password-reset-link"
                          onClick={() => {
                            window.history.pushState(
                              null,
                              "",
                              "/password-reset"
                            );
                            window.location.reload();
                          }}
                        >
                          비밀번호 재설정
                        </button>
                      </p>
                    </div>
                  </label>

                  <label className="mypage-row">
                    <span>새 비밀번호</span>

                    <div className="password-field-box">
                      <input
                        value={nextPassword}
                        onChange={(e) => setNextPassword(e.target.value)}
                        type="password"
                        placeholder="새 비밀번호를 입력해 주세요"
                      />

                      <ul className="password-rule-list">
                        <li className={hasPasswordLetter ? "is-pass" : ""}>
                          <span className="password-check-icon">
                            {hasPasswordLetter ? "✓" : ""}
                          </span>
                          영문 대/소문자 포함
                        </li>

                        <li className={hasPasswordNumber ? "is-pass" : ""}>
                          <span className="password-check-icon">
                            {hasPasswordNumber ? "✓" : ""}
                          </span>
                          숫자 포함
                        </li>

                        <li className={hasPasswordSpecial ? "is-pass" : ""}>
                          <span className="password-check-icon">
                            {hasPasswordSpecial ? "✓" : ""}
                          </span>
                          특수문자 포함
                        </li>

                        <li className={hasPasswordLength ? "is-pass" : ""}>
                          <span className="password-check-icon">
                            {hasPasswordLength ? "✓" : ""}
                          </span>
                          8자 이상
                        </li>
                      </ul>
                    </div>
                  </label>

                  <label className="mypage-row">
                    <span>새 비밀번호 확인</span>

                    <input
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      type="password"
                      placeholder="새 비밀번호를 다시 입력해 주세요"
                    />
                  </label>
                </div>

                <div className="mypage-actions mypage-actions-right">
                  <button className="secondary-button" type="button">
                    취소
                  </button>

                  <button
                    className={`mypage-submit-button ${
                      isPasswordValid ? "is-active" : ""
                    }`}
                    disabled={!isPasswordValid}
                    type="button"
                    onClick={handlePasswordSubmit}
                  >
                    변경 내용 저장
                  </button>
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

export default MyPage;