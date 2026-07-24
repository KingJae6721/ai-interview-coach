package com.aiinterview.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE("적절하지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED("지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND("대상을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),

    // Auth & Security
    UNAUTHORIZED("인증되지 않은 사용자입니다."),
    ACCESS_DENIED("접근 권한이 없습니다.");

    private final String message;
}
