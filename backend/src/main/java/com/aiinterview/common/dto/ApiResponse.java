package com.aiinterview.common.dto;

import com.aiinterview.common.code.ErrorCode;
import com.aiinterview.common.code.ResultCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 성공 응답 (데이터 없음)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, ResultCode.SUCCESS.name(), ResultCode.SUCCESS.getMessage(), null);
    }

    // 성공 응답 (데이터 포함)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, ResultCode.SUCCESS.name(), ResultCode.SUCCESS.getMessage(), data);
    }

    // 성공 응답 (사용자 지정 성공 코드 및 데이터 포함)
    public static <T> ApiResponse<T> success(ResultCode resultCode, T data) {
        return new ApiResponse<>(true, resultCode.name(), resultCode.getMessage(), data);
    }

    // 실패 응답 (ErrorCode 기반, 데이터 없음)
    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.name(), errorCode.getMessage(), null);
    }

    // 실패 응답 (ErrorCode 기반, 사용자 지정 메시지)
    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(false, errorCode.name(), customMessage, null);
    }

    // 실패 응답 (ErrorCode 기반, 데이터 포함)
    public static <T> ApiResponse<T> fail(ErrorCode errorCode, T data) {
        return new ApiResponse<>(false, errorCode.name(), errorCode.getMessage(), data);
    }
}
