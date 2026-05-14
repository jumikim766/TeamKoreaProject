import { useState } from 'react';
import '../styles/guide.css';
import {
  heroData,
  stepData,
  threatData,
  faqData,
  sectionTitles,
  resultExampleData,
  ctaData,
} from '../data/guideData';
import StepCard from '../components/StepCard';
import Header from '../components/Header';
import Navbar from '../components/Navbar';
import type { ViewMode } from '../App';

type ThemeMode = 'light' | 'dark';

interface GuidePageProps {
  theme: ThemeMode;
  isLoggedIn: boolean;
  onLogout: () => void;
  onToggleTheme: () => void;
  onGoHome: () => void;
  onGoLogin: () => void;
  onGoSignup: () => void;
  onGoMyPage: () => void;
  onNavigate: (view: ViewMode) => void;
}

const GuidePage = ({
  theme,
  isLoggedIn,
  onLogout,
  onToggleTheme,
  onGoHome,
  onGoLogin,
  onGoSignup,
  onGoMyPage,
  onNavigate,
}: GuidePageProps) => {
  const [openIndex, setOpenIndex] = useState<number | null>(null);

  const handleCtaClick = (button: string) => {
    if (button === 'URL 검사하기') {
      onNavigate('my-url');
      return;
    }

    if (button === '이메일 연동하기') {
      onNavigate('mail-connect');
      return;
    }

    if (button === '악성 URL 신고하기') {
      onNavigate('report');
    }
  };

  return (
    <div className={`guide-page ${theme}`}>
      <Header
        currentView="guide"
        theme={theme}
        isLoggedIn={isLoggedIn}
        onLogout={onLogout}
        onGoHome={onGoHome}
        onGoLogin={onGoLogin}
        onGoSignup={onGoSignup}
        onGoMyPage={onGoMyPage}
        onToggleTheme={onToggleTheme}
      />

      <Navbar onNavigate={onNavigate} />

      <main className="guide-main">
        <section className="hero-section">
          <div className="hero-badge">{heroData.badge}</div>

          <h1>{heroData.title}</h1>

          <p>{heroData.description}</p>

          <p className="hero-sub">{heroData.subDescription}</p>

          <div className="hero-buttons">
            <button
              type="button"
              className="primary-btn"
              onClick={() => onNavigate('my-url')}
            >
              {heroData.buttons.primary}
            </button>

            <button
              type="button"
              className="secondary-btn"
              onClick={() => onNavigate('mail-connect')}
            >
              {heroData.buttons.secondary}
            </button>
          </div>
        </section>

        <section className="guide-section">
          <h2>{sectionTitles.usage}</h2>

          <div className="card-grid">
            {stepData.map((step, index) => (
              <StepCard
                key={step.title}
                step={`0${index + 1}`}
                icon={step.icon}
                title={step.title}
                description={step.description}
              />
            ))}
          </div>
        </section>

        <section className="guide-section">
          <h2>{sectionTitles.threats}</h2>

          <div className="threat-list">
            {threatData.map((threat) => (
              <div className="threat-item" key={threat.title}>
                <div className="threat-icon">{threat.icon}</div>

                <h3>{threat.title}</h3>

                <p>{threat.description}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="guide-section">
          <h2>{sectionTitles.example}</h2>

          <div className="result-card">
            <div className="result-url">
              <span>URL</span>

              <p>{resultExampleData.url}</p>
            </div>

            <div className="risk-badge">{resultExampleData.risk}</div>

            <div className="result-reasons">
              <h3>{resultExampleData.reasonTitle}</h3>

              <ul>
                {resultExampleData.reasons.map((reason) => (
                  <li key={reason}>{reason}</li>
                ))}
              </ul>
            </div>
          </div>
        </section>

        <section className="guide-section">
          <h2>{sectionTitles.faq}</h2>

          <div className="faq-list">
            {faqData.map((faq, index) => (
              <div
                className={`faq-item ${openIndex === index ? 'active' : ''}`}
                key={faq.question}
                onClick={() => setOpenIndex(openIndex === index ? null : index)}
              >
                <div className="faq-question">
                  <h3>Q. {faq.question}</h3>

                  <span>{openIndex === index ? '−' : '+'}</span>
                </div>

                {openIndex === index && (
                  <p className="faq-answer">A. {faq.answer}</p>
                )}
              </div>
            ))}
          </div>
        </section>

        <section className="cta-section">
          <h2>{ctaData.title}</h2>

          <p>{ctaData.description}</p>

          <div className="cta-buttons">
            {ctaData.buttons.map((button) => (
              <button
                type="button"
                key={button}
                className="cta-btn"
                onClick={() => handleCtaClick(button)}
              >
                {button}
              </button>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
};

export default GuidePage;