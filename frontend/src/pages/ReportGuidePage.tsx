
import '../styles/reportGuide.css';
import { useState } from 'react';

import {
  heroData,
  sectionTitles,
  reportSteps,
  reportTypes,
  faqData,
} from '../data/reportGuideData';

const ReportGuidePage = () => {

  // FAQ state
  const [openIndex, setOpenIndex] = useState<number | null>(null);

  return (
    <div className="report-guide-page">

      {/* HERO */}
      <section className="hero-section">

        <div className="hero-badge">
          {heroData.badge}
        </div>

        <h1>
          {heroData.title}
        </h1>

        <p>
          {heroData.description}
        </p>

        <div className="hero-buttons">

          <button className="primary-btn">
            {heroData.buttons.primary}
          </button>

        </div>

      </section>

      {/* STEP UI (TIMELINE VERSION) */}
      <section className="guide-section">

        <h2>
          {sectionTitles.steps}
        </h2>

        <div className="timeline">

          {reportSteps.map((step, index) => (
            <div key={step.title} className="timeline-item">

              <div className="timeline-left">

                <div className="timeline-circle">
                  {index + 1}
                </div>

                {index !== reportSteps.length - 1 && (
                  <div className="timeline-line" />
                )}

              </div>

              <div className="timeline-content">

                <div className="timeline-icon">
                  {step.icon}
                </div>

                <h3>
                  {step.title}
                </h3>

                <p>
                  {step.description}
                </p>

              </div>

            </div>
          ))}

        </div>

      </section>

      {/* 신고 가능한 유형 */}
      <section className="guide-section">

        <h2>
          {sectionTitles.types}
        </h2>

        <div className="type-grid">

          {reportTypes.map((type) => (
            <div
              className="type-card"
              key={type.title}
            >

              <div className="type-icon">
                {type.icon}
              </div>

              <h3>
                {type.title}
              </h3>

            </div>
          ))}

        </div>

      </section>

      {/* FAQ (추가됨 🔥) */}
      <section className="guide-section">

        <h2>
            {sectionTitles.faq}
        </h2>

        <div className="faq-list">

          {faqData.map((item, index) => (
            <div
              key={index}
              className={`faq-item ${openIndex === index ? 'open' : ''}`}
              onClick={() =>
                setOpenIndex(openIndex === index ? null : index)
              }
            >

              <div className="faq-question">
                {item.question}
              </div>

              <div className="faq-answer">
                {item.answer}
              </div>

            </div>
          ))}

        </div>

      </section>

    </div>
  );
 
};

export default ReportGuidePage;