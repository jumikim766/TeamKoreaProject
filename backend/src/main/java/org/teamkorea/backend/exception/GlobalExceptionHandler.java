package org.teamkorea.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.teamkorea.backend.dto.BaseResponse;
import org.teamkorea.backend.exception.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //@Valid 검증 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        FieldError fieldError = e.getBindingResult().getFieldError();

        String message = (fieldError != null)
                ? fieldError.getDefaultMessage()
                : "요청한 값이 올바르지 않습니다. 입력값을 다시 확인해주세요.";

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(BaseResponse.error(message, ErrorCode.INVALID_INPUT.getCode()));
    }

    //잘못된 요청값 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        ErrorCode errorCode = resolveIllegalArgumentErrorCode(e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.error(e.getMessage(), errorCode.getCode()));
    }


    //중복 데이터 등 충돌 처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BaseResponse<Void>> handleIllegalStateException(
            IllegalStateException e
    ) {
        ErrorCode errorCode = resolveIllegalStateErrorCode(e.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.error(e.getMessage(), errorCode.getCode()));
    }


    //인증 실패 처리
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuthenticationException(
            AuthenticationException e
    ) {
        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED.getStatus())
                .body(BaseResponse.error("인증에 실패했습니다. 로그인 상태를 확인하거나 다시 로그인해주세요.", ErrorCode.UNAUTHORIZED.getCode()));
    }


    //권한 부족 처리

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDeniedException(
            AccessDeniedException e
    ) {
        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.getStatus())
                .body(BaseResponse.error("해당 요청에 대한 접근 권한이 없습니다.", ErrorCode.FORBIDDEN.getCode()));
    }

    //그 외 서버 오류 처리 
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(BaseResponse.error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", ErrorCode.INTERNAL_ERROR.getCode()));
    }

    private ErrorCode resolveIllegalArgumentErrorCode(String message) {
        if (message == null) {
            return ErrorCode.INVALID_INPUT;
        }

        if (message.contains("로그인이 필요") ||
                message.contains("인증") ||
                message.contains("비밀번호가 올바르지") ||
                message.contains("유효하지 않거나 만료된 refreshToken")) {
            return ErrorCode.UNAUTHORIZED;
        }

        if (message.contains("찾을 수 없습니다") ||
                message.contains("저장된 토큰 정보를 찾을 수 없습니다")) {
            return ErrorCode.NOT_FOUND;
        }

        if (message.contains("이미 사용 중") ||
                message.contains("이미 등록된")) {
            return ErrorCode.CONFLICT;
        }

        if (message.contains("누락") ||
                message.contains("올바르지") ||
                message.contains("부족합니다")) {
            return ErrorCode.INVALID_INPUT;
        }

        return ErrorCode.INVALID_INPUT;
    }

    private ErrorCode resolveIllegalStateErrorCode(String message) {
        if (message == null) {
            return ErrorCode.INVALID_INPUT;
        }

        if (message.contains("이미 사용 중") ||
                message.contains("이미 등록된")) {
            return ErrorCode.CONFLICT;
        }

        if (message.contains("소셜 로그인 계정")) {
            return ErrorCode.INVALID_INPUT;
        }

        if (message.contains("비활성화")) {
            return ErrorCode.INVALID_INPUT;
        }

        return ErrorCode.INVALID_INPUT;
    }
}