package org.teamkorea.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE) // ❗ 외부에서 new 금지
public class BaseResponse<T> {

    private boolean success;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorCode;

    /**
     * 성공 응답 (데이터 포함)
     */
    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data, null);
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    public static BaseResponse<Void> success(String message) {
        return new BaseResponse<>(true, message, null, null);
    }

    /**
     * 기본 에러 응답
     */
    public static BaseResponse<Void> error(String message) {
        return new BaseResponse<>(false, message, null, "COMMON_ERROR");
    }

    /**
     * 에러 코드 포함 응답
     */
    public static BaseResponse<Void> error(String message, String errorCode) {
        return new BaseResponse<>(false, message, null, errorCode);
    }
}