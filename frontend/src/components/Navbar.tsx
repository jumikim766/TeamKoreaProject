import type { ViewMode } from "../App";

type NavTarget =
  | "my-mailbox"
  | "mail-connect"
  | "url-statistics"
  | "my-url"
  | "url-library"
  | "notifications"
  | "notification-settings"
  | "report-guide"
  | "report"
  | "classification-method"
  | "classification-criteria";

interface NavbarProps {
  currentView?: ViewMode;
  onNavigate: (view: ViewMode) => void;
}

const navItems: {
  title: string;
  links: { label: string; view: NavTarget }[];
}[] = [
  {
    title: "메일함",
    links: [
      { label: "나의 메일함", view: "my-mailbox" },
      { label: "메일연동", view: "mail-connect" },
    ],
  },
  {
    title: "URL 관리",
    links: [
      { label: "URL 통계", view: "url-statistics" },
      { label: "나의 URL", view: "my-url" },
      { label: "전체 URL 모음", view: "url-library" },
    ],
  },
  {
    title: "알림함",
    links: [
      { label: "알림함", view: "notifications" },
      { label: "알림 설정", view: "notification-settings" },
    ],
  },
  {
    title: "신고하기",
    links: [
      { label: "신고 안내", view: "report-guide" },
      { label: "신고하기", view: "report" },
    ],
  },
  {
    title: "분류기준",
    links: [{ label: "분류 기준", view: "classification-criteria" }],
  },
];

function Navbar({ currentView, onNavigate }: NavbarProps) {
  return (
    <nav className="nav-shell" aria-label="주요 메뉴">
      <div className="nav">
        {navItems.map((item) => {
          const isActiveGroup = item.links.some((link) => link.view === currentView);

          return (
            <button
              key={item.title}
              className={isActiveGroup ? "nav-item is-active" : "nav-item"}
              type="button"
              onClick={() => onNavigate(item.links[0].view)}
            >
              <span>{item.title}</span>
            </button>
          );
        })}
      </div>

      <div className="mega-menu">
        {navItems.map((item) => (
          <section key={item.title} className="mega-column">
            <div className="mega-links">
              {item.links.map((link) => (
                <button
                  key={link.label}
                  type="button"
                  className={currentView === link.view ? "is-active" : ""}
                  onClick={() => onNavigate(link.view)}
                >
                  {link.label}
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
