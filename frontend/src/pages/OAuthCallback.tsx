import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { reissue } from "../api/authApi";
import { saveAccessToken } from "../utils/token";

function OAuthCallback() {
  const navigate = useNavigate();
  const handledRef = useRef(false);

  useEffect(() => {
    if (handledRef.current) return;
    handledRef.current = true;

    const handleLogin = async () => {
      try {
        // OAuth 성공 후 백엔드가 HttpOnly refreshToken 쿠키 발급
        // 프론트는 reissue() 호출로 accessToken 재발급
        const response = await reissue();

        const accessToken = response.data.data?.accessToken;

        if (!accessToken) {
          throw new Error("accessToken이 없습니다.");
        }

        // accessToken만 localStorage 저장
        saveAccessToken(accessToken);

        alert("소셜 로그인 성공");

        navigate("/");
      } catch (error) {
        console.error("OAuth 로그인 실패:", error);

        alert("로그인에 실패했습니다.");

        navigate("/login");
      }
    };

    handleLogin();
  }, [navigate]);

  return <div>로그인 처리 중...</div>;
}

export default OAuthCallback;