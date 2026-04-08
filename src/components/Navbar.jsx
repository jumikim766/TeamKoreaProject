function Navbar() {
  return (
    <nav className="nav">

      {/* 상단 메뉴 5개 */}
      <div className="nav-item">메일함</div>
      <div className="nav-item">URL 관리</div>
      <div className="nav-item">알림함</div>
      <div className="nav-item">신고하기</div>
      <div className="nav-item">분류기준</div>

      {/* ✅ 전체 펼쳐지는 메뉴 */}
      <div className="mega-menu">

        <div className="mega-column">
          <h4>메일함</h4>
          <p>나의 메일함</p>
          <p>메일 연동</p>
        </div>

        <div className="mega-column">
          <h4>URL 관리</h4>
          <p>나의 URL</p>
          <p>URL 모음</p>
        </div>

        <div className="mega-column">
          <h4>알림함</h4>
          <p>알림함</p>
          <p>알림 설정</p>
        </div>

        <div className="mega-column">
          <h4>신고하기</h4>
          <p>신고 안내</p>
          <p>신고하기</p>
        </div>

        <div className="mega-column">
          <h4>분류 기준</h4>
          <p>분류 방법</p>
          <p>분류기준</p>
        </div>

      </div>

    </nav>
  );
}

export default Navbar;
