package org.teamkorea.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.teamkorea.backend.dto.BaseResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** @Valid 검증 실패 처리 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = (fieldError != null)
                ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                : ErrorCode.INVALID_INPUT.getDefaultMessage();

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(BaseResponse.error(message, ErrorCode.INVALID_INPUT.getCode()));
    }

    /**
     * 비즈니스 로직 예외 처리 (핵심).
     * ErrorCode에 HTTP 상태코드가 이미 포함되어 있으므로
     * 메시지 문자열 분기 없이 상태코드를 결정한다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(
            BusinessException e
    ) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.error(e.getMessage(), errorCode.getCode()));
    }

    /** Spring Security 인증 실패 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuthenticationException(
            AuthenticationException e
    ) {
        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED.getStatus())
                .body(BaseResponse.error(
                        "인증에 실패했습니다. 로그인 상태를 확인하거나 다시 로그인해주세요.",
                        ErrorCode.UNAUTHORIZED.getCode()
                ));
    }

    /** Spring Security 권한 부족 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDeniedException(
            AccessDeniedException e
    ) {
        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.getStatus())
                .body(BaseResponse.error(
                        "해당 요청에 대한 접근 권한이 없습니다.",
                        ErrorCode.FORBIDDEN.getCode()
                ));
    }

    /** JWT details 파싱 실패 등 인증 객체 문제 */
    @ExceptionHandler(ClassCastException.class)
    public ResponseEntity<BaseResponse<Void>> handleClassCastException(
            ClassCastException e
    ) {
        return ResponseEntity
                .status(ErrorCode.UNAUTHORIZED.getStatus())
                .body(BaseResponse.error(
                        "인증 정보가 올바르지 않습니다. 다시 로그인해주세요.",
                        ErrorCode.UNAUTHORIZED.getCode()
                ));
    }

    /** 그 외 서버 오류 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(BaseResponse.error(
                        "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                        ErrorCode.INTERNAL_ERROR.getCode()
                ));
    }
}
