import { Cell, Pie, PieChart, ResponsiveContainer } from 'recharts';
import { getRiskColor } from '../utils/riskLevel';

interface ChartDatum {
  name: string;
  value: number;
}

interface ChartBoxProps {
  title: string;
  caption: string;
  total: string;
  data: ChartDatum[];
}


function ChartBox({ title, caption, total, data }: ChartBoxProps) {
  return (
    <section className="chart-card">
      <div className="chart-card-head">
        <div>
          <p className="chart-kicker">{title}</p>
          <h3>{total}</h3>
        </div>
        <p className="chart-caption">{caption}</p>
      </div>

      <div className="chart-content">
        <div className="chart-visual">
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie
                data={data}
                cx="50%"
                cy="50%"
                innerRadius={58}
                outerRadius={88}
                paddingAngle={3}
                dataKey="value"
                stroke="transparent"
                isAnimationActive={false}
              >
                {data.map((entry) => (
                  <Cell key={entry.name} fill={getRiskColor(entry.name)} />
                ))}
              </Pie>
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-legend">
          {data.map((item) => (
            <div key={item.name} className="legend-row">
              <div className="legend-main">
                <span
                  className="legend-dot"
                  style={{ backgroundColor: getRiskColor(item.name) }}
                />
                <span className="legend-label">{item.name}</span>
              </div>
              <strong>{item.value}건</strong>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

export default ChartBox;
