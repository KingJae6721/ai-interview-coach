package com.aiinterview.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "적절하지 않은 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // Auth & Security
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),

    // Company
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "회사를 찾을 수 없습니다."),

    // Job Position
    JOB_POSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "Job position not found."),

    // Job Posting
    JOB_POSTING_NOT_FOUND(HttpStatus.NOT_FOUND, "Job posting not found."),
    JOB_POSTING_NOT_ANALYZED(HttpStatus.CONFLICT, "Job posting analysis not found."),
    JOB_POSTING_POSITION_MISMATCH(HttpStatus.CONFLICT, "Job posting does not belong to the selected job position."),
    JOB_POSTING_URL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Job posting URL is not allowed."),
    JOB_POSTING_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "Job posting could not be fetched."),
    JOB_POSTING_CONTENT_NOT_FOUND(HttpStatus.UNPROCESSABLE_CONTENT, "Job posting content could not be extracted."),

    // Resume
    RESUME_INVALID_FILE(HttpStatus.BAD_REQUEST, "A valid PDF resume is required."),
    RESUME_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "Resume file exceeds the allowed size."),
    RESUME_TEXT_EXTRACTION_FAILED(HttpStatus.UNPROCESSABLE_CONTENT, "Resume text extraction failed."),
    RESUME_CONTENT_NOT_FOUND(HttpStatus.UNPROCESSABLE_CONTENT, "No readable text was found in the resume."),
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "Resume not found."),
    RESUME_ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have access to this resume."),
    RESUME_NOT_ANALYZED(HttpStatus.CONFLICT, "Resume analysis not found."),

    // Interview
    INTERVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "면접을 찾을 수 없습니다."),
    INTERVIEW_ALREADY_STARTED(HttpStatus.CONFLICT, "이미 시작된 면접입니다."),
    INTERVIEW_NOT_STARTED(HttpStatus.CONFLICT, "면접이 아직 시작되지 않았습니다."),
    INTERVIEW_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 면접입니다."),
    INTERVIEW_NOT_IN_PROGRESS(HttpStatus.CONFLICT, "진행 중인 면접이 아닙니다."),

    INTERVIEW_NOT_CANCELLABLE(HttpStatus.CONFLICT, "Only in-progress interviews can be cancelled."),
    INTERVIEW_ALREADY_CANCELLED(HttpStatus.CONFLICT, "This interview has already been cancelled."),
    INTERVIEW_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Interview question not found."),
    INTERVIEW_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "Interview answer not found."),
    INTERVIEW_ANSWER_ALREADY_EXISTS(HttpStatus.CONFLICT, "An answer already exists for this interview question."),
    ANSWER_ORDER_INVALID(HttpStatus.CONFLICT, "Only the next unanswered question can be answered."),

    INTERVIEW_NOT_COMPLETABLE(HttpStatus.CONFLICT, "All interview questions must be answered before completion."),
    INTERVIEW_NOT_COMPLETED(HttpStatus.CONFLICT, "Interview must be completed before feedback generation."),
    FEEDBACK_GENERATION_NOT_AVAILABLE(HttpStatus.CONFLICT, "Interview questions and answers are required."),
    PARTIAL_FEEDBACK_GENERATION_NOT_AVAILABLE(HttpStatus.CONFLICT,
            "At least two answers are required for partial feedback generation."),
    FEEDBACK_ALREADY_EXISTS(HttpStatus.CONFLICT, "Feedback already exists for this interview."),
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "Feedback not found."),
    QUESTION_EVALUATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "Question evaluation already exists for this answer."),
    QUESTION_EVALUATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Question evaluation not found."),

    // AI
    AI_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "AI 서버 요청에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
