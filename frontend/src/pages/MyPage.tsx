import { useState } from 'react';
import Header from '../components/Header';
import '../styles/Dashboard.css';

type ThemeMode = 'light' | 'dark';

type MyPageTab = 'profile' | 'password';

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

interface MyPageProps {
  theme: ThemeMode;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
  onNavigate: (view: PageViewTarget) => void;
}

function MyPage({
  theme,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
}: MyPageProps) {
  const [tab, setTab] = useState<MyPageTab>('profile');

  const [name, setName] = useState('XXX');
  const [userId, setUserId] = useState('abcd1234');
  const [phone, setPhone] = useState('010-****-****');
  const [email, setEmail] = useState('1234@5678.com');

  const [currentPassword, setCurrentPassword] = useState('');
  const [nextPassword, setNextPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const isProfileValid =
    name.trim().length >= 2 &&
    userId.trim().length >= 4 &&
    phone.trim().length >= 8 &&
    email.includes('@');

  const isPasswordValid =
    currentPassword.trim().length >= 1 &&
    nextPassword.trim().length >= 8 &&
    confirmPassword.trim().length >= 8 &&
    nextPassword === confirmPassword;

  return (
    <div className="dashboard-shell">
      <Header
        currentView="mypage"
        theme={theme}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onGoMyPage={onGoMyPage}
        onToggleTheme={onToggleTheme}
      />

      <main className="page-main">
        <div className="page-layout">
          <aside className="page-sidebar">
            <button className="back-button" onClick={onGoHome} type="button">
              뒤로가기
            </button>

            <div className="page-side-card">
              <div className="page-side-title">마이페이지</div>

              <button
                className={tab === 'profile' ? 'side-menu-button is-active' : 'side-menu-button'}
                onClick={() => setTab('profile')}
                type="button"
              >
                회원 정보 수정
              </button>

              <button
                className={tab === 'password' ? 'side-menu-button is-active' : 'side-menu-button'}
                onClick={() => setTab('password')}
                type="button"
              >
                비밀번호 변경
              </button>
            </div>
          </aside>

          <section className="page-content-card">
            {tab === 'profile' ? (
              <div className="mypage-section">
                <div className="mypage-head">
                  <p className="eyebrow">My page</p>
                  <h1>회원 정보 수정</h1>
                </div>

                <div className="mypage-form-grid">
                  <label className="mypage-row">
                    <span>이름</span>
                    <input value={name} onChange={(e) => setName(e.target.value)} type="text" />
                  </label>

                  <label className="mypage-row">
                    <span>아이디</span>
                    <div className="mypage-inline">
                      <input
                        value={userId}
                        onChange={(e) => setUserId(e.target.value)}
                        type="text"
                      />
                      <button className="secondary-button inline-action-button" type="button">
                        중복확인
                      </button>
                    </div>
                  </label>

                  <label className="mypage-row">
                    <span>전화번호</span>
                    <input value={phone} onChange={(e) => setPhone(e.target.value)} type="text" />
                  </label>

                  <label className="mypage-row">
                    <span>이메일</span>
                    <div className="mypage-inline">
                      <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" />
                      <button className="secondary-button inline-action-button" type="button">
                        중복확인
                      </button>
                    </div>
                  </label>
                </div>

                <div className="mypage-actions">
                  <button
                    className={`primary-button ${!isProfileValid ? 'is-disabled' : ''}`}
                    disabled={!isProfileValid}
                    type="button"
                  >
                    수정하기
                  </button>
                  <button className="secondary-button" type="button">
                    취소
                  </button>
                </div>
              </div>
            ) : (
              <div className="mypage-section">
                <div className="mypage-head">
                  <p className="eyebrow">My page</p>
                  <h1>비밀번호 변경</h1>
                </div>

                <div className="mypage-form-grid">
                  <label className="mypage-row">
                    <span>현재 비밀번호</span>
                    <input
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                      type="password"
                    />
                  </label>

                  <label className="mypage-row">
                    <span>새로운 비밀번호</span>
                    <input
                      value={nextPassword}
                      onChange={(e) => setNextPassword(e.target.value)}
                      type="password"
                    />
                  </label>

                  <label className="mypage-row">
                    <span>새로운 비밀번호 확인</span>
                    <input
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      type="password"
                    />
                  </label>
                </div>

                <div className="mypage-actions">
                  <button
                    className={`primary-button ${!isPasswordValid ? 'is-disabled' : ''}`}
                    disabled={!isPasswordValid}
                    type="button"
                  >
                    변경하기
                  </button>
                  <button className="secondary-button" type="button">
                    취소
                  </button>
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

export default MyPage;