export type RiskLevel = 'SAFE' | 'SUSPICIOUS' | 'WARNING' | 'DANGER' | 'CRITICAL';

export type RiskLevelLabel = '안전' | '의심' | '주의' | '위험' | '심각';

export const RISK_LABEL_MAP: Record<RiskLevel, RiskLevelLabel> = {
  SAFE: '안전',
  SUSPICIOUS: '의심',
  WARNING: '주의',
  DANGER: '위험',
  CRITICAL: '심각',
};

export const RISK_COLOR_MAP: Record<RiskLevel, string> = {
  SAFE: '#2478FF',
  SUSPICIOUS: '#FFF612',
  WARNING: '#FFBB00',
  DANGER: '#FF7012',
  CRITICAL: '#FF1212',
};

export const normalizeRiskLevel = (value?: string | null): RiskLevel => {
  switch (value) {
    case 'SAFE':
    case '안전':
      return 'SAFE';

    case 'SUSPICIOUS':
    case '의심':
      return 'SUSPICIOUS';

    case 'WARNING':
    case 'CAUTION':
    case '주의':
      return 'WARNING';

    case 'DANGER':
    case 'DANGEROUS':
    case '위험':
      return 'DANGER';

    case 'CRITICAL':
    case '심각':
    case '매우 위험':
      return 'CRITICAL';

    default:
      return 'SAFE';
  }
};

export const getRiskLabel = (value?: string | null): RiskLevelLabel => {
  return RISK_LABEL_MAP[normalizeRiskLevel(value)];
};

export const getRiskColor = (value?: string | null): string => {
  return RISK_COLOR_MAP[normalizeRiskLevel(value)];
};

export const getRiskClassName = (value?: string | null): string => {
  return `risk-${normalizeRiskLevel(value).toLowerCase()}`;
};
