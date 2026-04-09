interface SummaryCardProps {
  title: string;
  value: string;
  change: string;
  tone: 'critical' | 'steady' | 'positive';
}

function SummaryCard({ title, value, change, tone }: SummaryCardProps) {
  return (
    <article className={`summary-card summary-${tone}`}>
      <div>
        <p className="summary-title">{title}</p>
        <h3>{value}</h3>
      </div>
      <span className="summary-change">{change}</span>
    </article>
  );
}

export default SummaryCard;
