import Header from '../components/Header';
import Navbar from '../components/Navbar';
import SummaryCard from '../components/SummaryCard';
import ChartBox from '../components/ChartBox';
import '../styles/Dashboard.css';

function Dashboard() {
  return (
    <div className="container">

      {/* 상단 */}
      <Header />
      <Navbar />

      {/* 메인 */}
      <div className="main">

        {/* 왼쪽 */}
        <div className="left-box">
          <h2>웹사이트 설명</h2>
        </div>

        {/* 오른쪽 */}
        <div className="right-box">

          {/* 전체 수집 */}
          <div>
            <SummaryCard title="전체 수집 URL" />
            <ChartBox />
          </div>

          {/* 오늘 수집 */}
          <div>
            <SummaryCard title="오늘 수집 URL" />
            <ChartBox />
          </div>

        </div>

      </div>

      {/* 하단 */}
      <footer className="footer">
        서비스 소개 | 이용약관
      </footer>

    </div>
  );
}

export default Dashboard;
