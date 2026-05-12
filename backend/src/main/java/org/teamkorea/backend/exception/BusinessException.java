package org.teamkorea.backend.exception;

/**
 * 비즈니스 로직 예외.
 * ErrorCode를 직접 포함하므로 GlobalExceptionHandler에서
 * 문자열 키워드 분기 없이 HTTP 상태코드와 에러코드를 결정
 *
 * 사용 예)
 *   throw new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
 *   throw new BusinessException(ErrorCode.CONFLICT, "이미 사용 중인 아이디입니다.");
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /** ErrorCode 기본 메시지 사용 */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    /** 커스텀 메시지 사용 */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
