import { useEffect, useRef } from "react";
import { reissue } from "../api/authApi";
import { clearAccessToken, saveAccessToken } from "../utils/token";

type OAuthCallbackPageProps = {
  onSuccess: () => void;
  onFail: () => void;
};

function OAuthCallbackPage({ onSuccess, onFail }: OAuthCallbackPageProps) {
  const handledRef = useRef(false);

  useEffect(() => {
    if (handledRef.current) return;
    handledRef.current = true;

    const handleOAuthCallback = async () => {
      try {
        // 백엔드가 OAuth 성공 시 refreshToken을 HttpOnly 쿠키로 내려줌
        // 프론트는 쿠키를 직접 읽지 않고 reissue()로 accessToken만 재발급받음
        const response = await reissue();

        const accessToken = response.data.data?.accessToken;

        if (!accessToken) {
          throw new Error("accessToken이 없습니다.");
        }

        saveAccessToken(accessToken);

        onSuccess();

        alert("로그인이 성공되었습니다.");
        window.location.replace("/");
      } catch (error) {
 clearAccessToken();

  onFail();

  alert("로그인에 실패했습니다.");
  window.location.replace("/login");
}
    };

    handleOAuthCallback();
  }, [onSuccess, onFail]);

  return <div>로그인 처리 중...</div>;
}

export default OAuthCallbackPage;