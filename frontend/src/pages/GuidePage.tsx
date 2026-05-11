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


const GuidePage = () => {
    const [openIndex, setOpenIndex] = 
    useState<number | null>(null);
  return (
    <div className="guide-page">
      <section className="hero-section">
        <div className="hero-badge">{heroData.badge}</div>

      <h1>{heroData.title}</h1>

        <p>{heroData.description}</p>

        <p className="hero-sub">
  {heroData.subDescription}
</p>

        <div className="hero-buttons">
        <button className="primary-btn">
             {heroData.buttons.primary}
        </button>

        <button className="secondary-btn">
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
        <div
        className="threat-item"
        key={threat.title}
        >

        <div className="threat-icon">
            {threat.icon}
        </div>

        <h3>
            {threat.title}
        </h3>

        <p>
            {threat.description}
        </p>

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

    <div className="risk-badge">
      {resultExampleData.risk}
    </div>

    <div className="result-reasons">

        <h3>
        {resultExampleData.reasonTitle}
        </h3>

      <ul>

        {resultExampleData.reasons.map((reason) => (
          <li key={reason}>
            {reason}
          </li>
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
    className={`faq-item ${
      openIndex === index ? 'active' : ''
    }`}
    key={faq.question}
    onClick={() =>
      setOpenIndex(
        openIndex === index ? null : index
      )
    }
  >

    <div className="faq-question">

      <h3>
        Q. {faq.question}
      </h3>

      <span>
        {openIndex === index ? '−' : '+'}
      </span>

    </div>

    {openIndex === index && (
      <p className="faq-answer">
        A. {faq.answer}
      </p>
    )}

  </div>
))}

  </div>

</section>

<section className="cta-section">

  <h2>
    {ctaData.title}
  </h2>

  <p>
    {ctaData.description}
  </p>

  <div className="cta-buttons">

    {ctaData.buttons.map((button) => (
      <button
        key={button}
        className="cta-btn"
      >
        {button}
      </button>
    ))}

  </div>

</section>

    </div>
  );
};

export default GuidePage;