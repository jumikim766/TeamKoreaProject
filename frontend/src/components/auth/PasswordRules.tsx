type PasswordRulesProps = {
  password: string;
};

function PasswordRules({ password }: PasswordRulesProps) {
  const rules = [
    {
      label: "대/소문자 포함",
      valid: /(?=.*[a-z])(?=.*[A-Z])/.test(password),
    },
    {
      label: "숫자 포함",
      valid: /[0-9]/.test(password),
    },
    {
      label: "특수문자 포함",
      valid: /[^A-Za-z0-9]/.test(password),
    },
    {
      label: "8자 이상",
      valid: password.length >= 8,
    },
  ];

  return (
    <ul className="login-password-rules">
      {rules.map((rule) => (
        <li key={rule.label} className={rule.valid ? "active" : ""}>
          {rule.valid && (
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="3"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="password-rule-check"
            >
              <path d="M20 6 9 17l-5-5" />
            </svg>
          )}
          <span>{rule.label}</span>
        </li>
      ))}
    </ul>
  );
}

export default PasswordRules;