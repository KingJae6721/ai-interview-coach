package com.aiinterview.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {
    SUCCESS("요청이 성공적으로 처리되었습니다."),
    CREATED("자원이 성공적으로 생성되었습니다.");

    private final String message;
}
