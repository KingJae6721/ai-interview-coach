# 05. API Specification

## Base URL

```
/api/v1
```

---

# Authentication

JWT Access Token을 Authorization Header에 포함한다.

```
Authorization: Bearer {accessToken}
```

---

# Response Format

모든 응답은 아래 형식을 따른다.

```json
{
  "success": true,
  "data": {},
  "message": "Success"
}
```

실패 시

```json
{
  "success": false,
  "message": "Invalid Request"
}
```

---

# Authentication API

## 회원가입

### POST /api/v1/auth/signup

인증이 필요하지 않은 공개 API입니다.

#### Request

```json
POST /api/v1/auth/signup
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "Password1!",
    "nickname": "홍길동"
}
```

#### Validation 규칙

| 필드 | 제약 조건 |
|---|---|
| email | 필수, 이메일 형식, 최대 255자 |
| password | 필수, 8~20자, 영문+숫자+특수문자 포함 |
| nickname | 필수, 2~50자 |

#### Success Response

HTTP Status: **201 Created**

```json
{
    "success": true,
    "code": "USER_CREATED",
    "message": "회원가입이 완료되었습니다.",
    "data": {
        "id": 1,
        "email": "user@example.com",
        "nickname": "홍길동"
    }
}
```

#### Error Response - 이메일 중복

HTTP Status: **409 Conflict**

```json
{
    "success": false,
    "code": "DUPLICATE_EMAIL",
    "message": "이미 사용 중인 이메일입니다.",
    "data": null
}
```

#### Error Response - Validation 실패

HTTP Status: **400 Bad Request**

```json
{
    "success": false,
    "code": "INVALID_INPUT_VALUE",
    "message": "적절하지 않은 입력값입니다.",
    "data": [
        {
            "field": "email",
            "rejectedValue": "not-email",
            "reason": "올바른 이메일 형식이 아닙니다."
        },
        {
            "field": "password",
            "rejectedValue": "1234",
            "reason": "비밀번호는 8자 이상, 영문/숫자/특수문자를 포함해야 합니다."
        }
    ]
}
```

#### HTTP Status 정리

| 상황 | Status | Code |
|---|---|---|
| 회원가입 성공 | 201 Created | USER_CREATED |
| Validation 실패 | 400 Bad Request | INVALID_INPUT_VALUE |
| 이메일 중복 | 409 Conflict | DUPLICATE_EMAIL |
| 서버 오류 | 500 Internal Server Error | INTERNAL_SERVER_ERROR |

---

## 로그인

### POST /auth/login

### Request

```json
{
  "email": "test@test.com",
  "password": "1234"
}
```

### Response

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

---

## 로그아웃

### POST /auth/logout

### Header

```
Authorization: Bearer AccessToken
```

### Response

```json
{
  "message":"Logout Success"
}
```

---

# Resume API

## 이력서 업로드

### POST /resumes

### Request

multipart/form-data

```
file : resume.pdf
```

### Response

```json
{
  "resumeId": 1,
  "fileUrl": "...",
  "status": "UPLOADED"
}
```

---

## 이력서 조회

### GET /resumes

### Response

```json
[
  {
    "id":1,
    "fileName":"resume.pdf",
    "createdAt":"..."
  }
]
```

---

# Company API

## 회사 목록

### GET /companies

---

## 회사 상세

### GET /companies/{companyId}

---

# Job Posting API

## 채용공고 등록

### POST /job-postings

### Request

```json
{
  "companyId":1,
  "url":"https://..."
}
```

### Process

```
URL 입력

↓

본문 수집

↓

AI 분석

↓

DB 저장
```

### Response

```json
{
  "jobPostingId":1,
  "company":"네이버",
  "position":"Backend"
}
```

---

## 채용공고 조회

### GET /job-postings/{id}

---

# Interview API

## 면접 생성

### POST /interviews

### Request

```json
{
  "jobPositionId": 1,
  "title": "Backend Interview"
}
```

### Process

```
Company + JobPosition

Question distribution policy (difficulty and category)

↓

Prompt 생성

↓

OpenAI

↓

Question 생성

↓

Question 저장
```

### Response

```json
{
  "interviewId":10,
  "questionCount":10,
  "status":"READY"
}
```

---

## 면접 시작

### POST /interviews/{id}/start

### Response

```json
{
  "status":"IN_PROGRESS"
}
```

---

## 면접 종료

### POST /interviews/{id}/finish

### Response

```json
{
  "status":"COMPLETED"
}
```

---

## 내 면접 목록

### GET /interviews

---

## 면접 상세

### GET /interviews/{id}

---

# Question API

## 질문 조회

### GET /interviews/{id}/questions

### Response

```json
[
  {
    "id":1,
    "content":"JPA의 N+1 문제를 설명하세요.",
    "category":"TECH",
    "order":1
  }
]
```

---

## 다음 질문

### GET /questions/{id}/next

---

# Answer API

## 답변 제출

### POST /questions/{questionId}/answers

### Request

```json
{
  "content":"N+1 문제는..."
}
```

### Process

```
Answer 저장

↓

OpenAI 평가

↓

Feedback 생성
```

### Response

```json
{
  "answerId":1,
  "feedbackCreated":true
}
```

---

## 답변 조회

### GET /answers/{id}

---

# AI Follow-up Question API

## POST /api/v1/ai/questions/{questionId}/follow-up

The authenticated interview owner can generate at most one follow-up question from the saved answer.

### Success Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "parentQuestionId": 1,
    "followUpQuestionId": 6,
    "content": "해당 선택이 성능에 미친 영향을 구체적으로 설명해 주세요.",
    "created": true
  }
}
```

---

# Feedback API

## 피드백 조회

### GET /answers/{answerId}/feedback

### Response

```json
{
  "totalScore":85,

  "technicalScore":90,

  "logicScore":80,

  "communicationScore":75,

  "specificityScore":95,

  "strength":"프로젝트 경험 설명이 좋습니다.",

  "weakness":"답변이 다소 추상적입니다.",

  "recommendation":"Transaction을 학습하세요."
}
```

---

## Interview Feedback Generate

### POST /interviews/{interviewId}/feedback

Completed interview owners can generate one aggregate AI feedback result.

### Success Response

```json
{
  "success": true,
  "code": "AI_FEEDBACK_COMPLETED",
  "message": "AI feedback completed",
  "data": {
    "feedbackId": 1,
    "interviewId": 10,
    "overallScore": 85,
    "strengths": "...",
    "weaknesses": "...",
    "improvementSuggestions": "...",
    "summary": "..."
  }
}
```

---

## Interview Result Query

### GET /interviews/{interviewId}/result

Only the completed interview owner can retrieve the questions, submitted answers, and aggregate feedback.

### Success Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "interviewId": 10,
    "title": "Backend Interview",
    "status": "COMPLETED",
    "questionAnswers": [
      {
        "questionOrder": 1,
        "questionContent": "...",
        "answerContent": "...",
        "answeredAt": "2026-08-06T10:00:00"
      }
    ],
    "feedback": {
      "overallScore": 85,
      "strengths": "...",
      "weaknesses": "...",
      "improvementSuggestions": "...",
      "summary": "..."
    }
  }
}
```

---

# Dashboard API

## 내 통계

### GET /dashboard

### Response

```json
{
  "interviewCount":15,

  "averageScore":82,

  "bestCategory":"TECH",

  "weakCategory":"CS"
}
```

---

# AI API

## 질문 재생성

### POST /ai/questions/regenerate

### Request

```json
{
  "interviewId":1
}
```

---

## 꼬리 질문 생성

### POST /api/v1/ai/questions/{questionId}/follow-up

The saved answer is used as the sole input for follow-up question generation.

---

## 답변 재평가

### POST /ai/review

### Request

```json
{
  "answerId":1
}
```

---

# HTTP Status

|Code|Description|
|------|----------------|
|200|Success|
|201|Created|
|204|No Content|
|400|Bad Request|
|401|Unauthorized|
|403|Forbidden|
|404|Not Found|
|409|Conflict|
|500|Internal Server Error|

---

# Exception Response

```json
{
  "success":false,
  "message":"Resume Not Found"
}
```
