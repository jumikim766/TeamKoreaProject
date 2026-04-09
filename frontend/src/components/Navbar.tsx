const navItems = [
  { title: '메일함', links: ['수집 현황', '연동 계정', '필터 정책'] },
  { title: 'URL 관리', links: ['위험 URL', '검토 대기', '허용 목록'] },
  { title: '알림함', links: ['실시간 경보', '보고서 예약', 'Webhook 연동'] },
  { title: '신고하기', links: ['신고 접수', '조치 내역', '대응 가이드'] },
  { title: '분류 기준', links: ['위험도 모델', '탐지 규칙', '감사 로그'] },
];

function Navbar() {
  return (
    <nav className="nav-shell" aria-label="주요 메뉴">
      <div className="nav">
        {navItems.map((item) => (
          <button key={item.title} className="nav-item" type="button">
            <span>{item.title}</span>
            <span className="nav-badge">{item.links.length}</span>
          </button>
        ))}
      </div>

      <div className="mega-menu">
        {navItems.map((item) => (
          <section key={item.title} className="mega-column">
            <p className="mega-label">{item.title}</p>
            <h4>{item.links[0]}</h4>
            <div className="mega-links">
              {item.links.map((link) => (
                <button key={link} type="button">
                  {link}
                </button>
              ))}
            </div>
          </section>
        ))}
      </div>
    </nav>
  );
}

export default Navbar;
