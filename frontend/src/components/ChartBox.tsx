import { PieChart, Pie, Cell } from 'recharts';

const data = [
  { name: '매우 위험', value: 20 },
  { name: '위험', value: 10 },
  { name: '주의', value: 40 },
  { name: '안전', value: 30 },
];

const COLORS = ['#ff4d4f', '#fa8c16', '#52c41a', '#1890ff'];

function ChartBox() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '40px' }}>
      
      {/* 그래프 */}
      <PieChart width={220} height={220}>
        <Pie
          data={data}
          cx="50%"
          cy="50%"
          outerRadius={90}
          dataKey="value"
        >
          {data.map((entry, index) => (
            <Cell key={index} fill={COLORS[index]} />
          ))}
        </Pie>
      </PieChart>

      {/* ✅ 배지 형태 텍스트 */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {data.map((item, index) => (
          <div key={index} style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            
            {/* ✅ 글씨 감싸는 네모 */}
            <div
              style={{
                backgroundColor: COLORS[index],
                padding: '6px 12px',
                borderRadius: '0px',
                color: 'white',
                fontWeight: 'bold'
              }}
            >
              {item.name}
            </div>

            {/* ✅ 개수 */}
            <span>{item.value} 개</span>

          </div>
        ))}
      </div>

    </div>
  );
}

export default ChartBox;
