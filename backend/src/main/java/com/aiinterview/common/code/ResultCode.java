package com.aiinterview.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {
    // 범용
    SUCCESS("요청이 성공적으로 처리되었습니다."),
    CREATED("자원이 성공적으로 생성되었습니다."),

    // Auth
    USER_CREATED("회원가입이 완료되었습니다."),
    LOGIN_SUCCESS("로그인이 완료되었습니다."),
    TOKEN_REISSUED("토큰이 재발급되었습니다."),

    // Interview
    INTERVIEW_CREATED("면접이 생성되었습니다."),
    QUESTION_GENERATED("질문이 생성되었습니다."),
    AI_FEEDBACK_COMPLETED("AI 피드백이 완료되었습니다.");

    private final String message;
}

