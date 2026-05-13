import Header from "../components/Header";
import Navbar from "../components/Navbar";
import type { ViewMode } from "../App";
import termsData from "../data/terms.json";
import "../styles/Policy.css";
import { useEffect } from "react";

type ThemeMode = "light" | "dark";

type TermsPageProps = {
  theme: ThemeMode;
  isLoggedIn: boolean;
  userName?: string;
  onLogout: () => void;
  onNavigate: (view: ViewMode) => void;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
};

type Chapter = {
  chapterId: string;
  chapterTitle: string;
  sections: Section[];
};

type Section = {
  sectionId: string;
  sectionTitle: string;
  sectionContent: string[];
};

const chapters: Chapter[] = termsData as Chapter[];

function scrollToId(id: string) {
  const el = document.getElementById(id);
  if (!el) return;
  const offset = 140;
  const top = el.getBoundingClientRect().top + window.scrollY - offset;
  window.scrollTo({ top, behavior: "smooth" });
}

function TermsPage({
  theme,
  isLoggedIn,
  userName,
  onLogout,
  onNavigate,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
}: TermsPageProps) {
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  return (
    <div className={`dashboard-shell ${theme}`}>
      <Header
        theme={theme}
        currentView="terms"
        isLoggedIn={isLoggedIn}
        userName={userName}
        onLogout={onLogout}
        onToggleTheme={onToggleTheme}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onGoMyPage={onGoMyPage}
      />

      <Navbar onNavigate={onNavigate} />

      <main className="policy-page">
        {/* 페이지 제목 */}
        <div className="policy-heading">
          <button className="back-button" onClick={onGoHome} type="button">
            뒤로가기
          </button>
          <p className="policy-eyebrow">URL GUARD</p>
          <h1 className="policy-title">이용약관</h1>
        </div>

        {/* 목차 박스 */}
        <div className="policy-toc-box">
          <div className="policy-toc-grid">
            {chapters.map((ch) => (
              <div key={ch.chapterId} className="policy-toc-col">
                <button
                  type="button"
                  className="policy-toc-chapter"
                  onClick={() => scrollToId(ch.chapterId)}
                >
                  {ch.chapterTitle}
                </button>
                {ch.sections.map((sec) => (
                  <button
                    key={sec.sectionId}
                    type="button"
                    className="policy-toc-section"
                    onClick={() => scrollToId(sec.sectionId)}
                  >
                    {sec.sectionTitle}
                  </button>
                ))}
              </div>
            ))}
          </div>
        </div>

        {/* 본문 */}
        <div className="policy-body">
          {chapters.map((ch) => (
            <section
              key={ch.chapterId}
              id={ch.chapterId}
              className="policy-chapter"
            >
              <h2 className="policy-chapter-title">{ch.chapterTitle}</h2>

              {ch.sections.map((sec) => (
                <div
                  key={sec.sectionId}
                  id={sec.sectionId}
                  className="policy-section"
                >
                  <h3 className="policy-section-title">{sec.sectionTitle}</h3>
                  <div className="policy-section-body">
                    {sec.sectionContent.map((line, idx) => (
                      <p key={idx} className="policy-line">
                        {line}
                      </p>
                    ))}
                  </div>
                </div>
              ))}
            </section>
          ))}
        </div>
      </main>
    </div>
  );
}

export default TermsPage;
