export const heroData = {
  badge: 'REPORT SECURITY CENTER',

  title: `악성 URL
신고 안내`,

  description: `의심되는 URL을 신고하여
다른 사용자의 피해를 예방하세요.`,

  buttons: {
    primary: 'URL 신고하기',
  },
};

export const sectionTitles = {
  steps: '신고 절차',
  types: '신고 가능한 유형',
  example: '신고 예시',
  faq: 'FAQ',
  cta: '의심되는 URL을 발견했다면 지금 바로 신고하세요.',
};

export const reportSteps = [
  {
    icon: '🔗',
    title: 'URL 입력',
    description: '의심 링크를 입력합니다.',
  },

  {
    icon: '⚠️',
    title: '위험 정보 작성',
    description: '신고 사유를 입력합니다.',
  },

  {
    icon: '🛡️',
    title: '관리자 검토',
    description: '보안팀이 신고 내용을 확인합니다.',
  },

  {
    icon: '✅',
    title: '결과 반영',
    description: '위험 URL DB에 반영됩니다.',
  },
];

export const reportTypes = [
  {
    icon: '🎣',
    title: '피싱 사이트',
  },

  {
    icon: '📱',
    title: '스미싱 문자',
  },

  {
    icon: '💀',
    title: '악성 다운로드 링크',
  },

  {
    icon: '🌐',
    title: '사칭 도메인',
  },

  {
    icon: '📢',
    title: '광고 스팸',
  },
];

export const reportExample = {
  url: 'https://fake-bank-login.com',

  reasons: [
    '금융기관 사칭',
    '개인정보 탈취 유도',
  ],
};

export const faqData = [
  {
    question: '익명 신고가 가능한가요?',
    answer: '일부 신고는 익명으로 접수 가능합니다.',
  },

  {
    question: '신고 후 얼마나 걸리나요?',
    answer: '관리자 검토 후 순차적으로 처리됩니다.',
  },

  {
    question: '잘못 신고하면 어떻게 되나요?',
    answer: '검토 과정에서 정상 URL 여부를 다시 확인합니다.',
  },
];

export const ctaData = {
  title: '의심되는 URL을 발견했다면 지금 바로 신고하세요.',

  description: `AI 기반 탐지 시스템이
악성 URL과 피싱 위험으로부터 사용자를 보호합니다.`,

  buttons: [
    'URL 신고',
    '문의하기',
  ],
};