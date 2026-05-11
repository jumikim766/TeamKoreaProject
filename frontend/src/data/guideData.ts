export const heroData = {
  badge: 'AI SECURITY SYSTEM',

  title: `AI 기반
악성 URL 탐지 시스템`,

  description: `이메일과 웹에서 수집된 URL을 자동 분석하여
피싱 · 스미싱 · 악성코드 · 의심 링크를 탐지합니다.`,

  subDescription: `AI 및 규칙 기반 분석 엔진이
URL 위험도를 실시간으로 분류하고 사용자에게 경고합니다.`,

  buttons: {
    primary: 'URL 검사하기',

    secondary: '이메일 연동',
  },
};

export const stepData = [
  {
    icon: '📩',
    title: '이메일 연동 또는 URL 입력',

    description:
      '사용자는 이메일 계정을 연동하거나 의심되는 URL을 직접 입력할 수 있습니다.',
  },

  {
    icon: '🤖',
    title: 'URL 자동 수집 및 AI 분석',

    description:
      '시스템이 이메일 본문에서 URL을 자동 추출하고 AI 탐지 엔진이 위험 여부를 분석합니다.',
  },

  {
    icon: '🛡️',
    title: '분석 결과 및 위험도 확인',

    description:
      '위험 등급, 탐지 사유, 분석 결과를 확인할 수 있으며 위험 URL 발견 시 알림을 받을 수 있습니다.',
  },
];

export const threatData = [
  {
    icon: '🎣',
    title: '피싱 사이트',
    description: '로그인 정보 탈취',
  },

  {
    icon: '📱',
    title: '스미싱 링크',
    description: '문자 기반 사기',
  },

  {
    icon: '🦠',
    title: '악성코드 URL',
    description: '바이러스 유포',
  },

  {
    icon: '🔗',
    title: '단축 URL 악용',
    description: '실제 주소를 숨겨 위험 사이트로 연결',
  },

  {
    icon: '📢',
    title: '광고성 스팸',
    description: '강제 리디렉션',
  },

  {
    icon: '🎭',
    title: '도메인 사칭',
    description: '정상 사이트와 유사한 주소로 사용자 혼란 유발',
  },
];

export const faqData = [
  {
    question: '이메일 연동은 안전한가요?',

    answer:
      '시스템은 URL 분석 목적으로만 이메일 데이터를 처리하며 민감 정보는 저장하지 않습니다.',
  },

  {
    question: '모든 URL이 자동 검사되나요?',

    answer:
      '이메일 내 URL 및 사용자가 입력한 URL을 자동 분석합니다.',
  },

  {
    question: '위험 URL 발견 시 어떻게 되나요?',

    answer:
      '위험도 경고 및 탐지 사유를 사용자에게 제공합니다.',
  },
];

export const sectionTitles = {
  usage: '사용 방법',

  threats: '탐지 가능한 위협',

  example: '검사 결과 예시',

  faq: 'FAQ',

  cta: '의심되는 링크를 지금 바로 검사해보세요.',
};

export const resultExampleData = {
  url: 'http://fake-login-security.com',

  risk: 'HIGH RISK',

  reasonTitle: '탐지 사유',

  reasons: [
    '피싱 로그인 페이지 의심',
    '정상 사이트와 유사한 도메인 사용',
    '개인정보 입력 유도 가능성',
  ],
};

export const ctaData = {
  title: '의심되는 링크를 지금 바로 검사해보세요.',

  description: `AI 기반 탐지 시스템이
악성 URL과 피싱 위험으로부터 사용자를 보호합니다.`,

  buttons: [
    'URL 검사하기',
    '이메일 연동하기',
    '악성 URL 신고하기',
  ],
};