export type RiskLevel =
  | 'SAFE'
  | 'SUSPICIOUS'
  | 'WARNING'
  | 'DANGER'
  | 'CRITICAL'
  | '안전'
  | '주의'
  | '위험';

export function getRiskLabel(level: string) {
  switch (level) {
    case 'SAFE':
    case '안전':
      return '안전';

    case 'SUSPICIOUS':
    case 'WARNING':
    case '주의':
      return '주의';

    case 'DANGER':
    case 'CRITICAL':
    case '위험':
      return '위험';

    default:
      return level;
  }
}

export function getRiskColor(level: string) {
  const label = getRiskLabel(level);

  switch (label) {
    case '안전':
      return '#2478FF';

    case '주의':
      return '#FFBB00';

    case '위험':
      return '#FF1212';

  }
}

export function getRiskClassName(level: string) {
  const label = getRiskLabel(level);

  switch (label) {
    case '안전':
      return 'risk-safe';

    case '주의':
      return 'risk-warning';

    case '위험':
      return 'risk-danger';

    default:
      return '';
  }
}