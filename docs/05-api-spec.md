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

## 이력서 PDF 업로드 및 분석

### POST /api/v1/resumes/analyze

### Request

multipart/form-data

```
file: resume.pdf (PDF, maximum 5MB)
```

### Response

```json
{
  "success": true,
  "code": "CREATED",
  "data": {
    "resumeId": 1,
    "originalFileName": "resume.pdf",
    "fileSize": 123456,
    "summary": "...",
    "skills": ["Java", "Spring Boot"],
    "workExperiences": ["..."],
    "projects": ["..."],
    "education": [],
    "certifications": [],
    "achievements": ["Latency reduced by 30%"],
    "strengths": ["Backend API design"],
    "keywords": ["backend", "transactions"],
    "analyzedAt": "2026-08-26T15:30:00"
  }
}
```

---

## 이력서 조회

### GET /api/v1/resumes

### Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "data": [{
    "resumeId": 1,
    "originalFileName": "resume.pdf",
    "createdAt": "2026-08-26T15:30:00",
    "summary": "...",
    "skills": ["Java", "Spring Boot"]
  }]
}
```

The original PDF binary is not persisted in the MVP. Only sanitized metadata, SHA-256, extracted text, and the AI
analysis snapshot are stored. Scanned/image-only PDFs are not supported because OCR is outside this Sprint.

---

# Company API

## 회사 목록

### GET /companies

---

## 회사 상세

### GET /companies/{companyId}

---

# JobPosition API

## 직무 목록 조회

### GET /api/v1/job-positions

로그인 사용자가 면접 생성에 사용할 전체 직무 목록을 조회한다.

### Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": [
    {
      "jobPositionId": 1,
      "positionName": "Backend Developer",
      "companyId": 1,
      "companyName": "AI Interview",
      "techStack": ["Java", "Spring Boot"]
    }
  ]
}
```

JobPosition이 없으면 `data`는 빈 배열을 반환한다.

---

# Job Posting API

## 채용공고 URL 분석

### POST /api/v1/job-postings/analyze

### Request

```json
{
  "jobPositionId": 1,
  "postingUrl": "https://careers.example.com/jobs/backend-developer"
}
```

### Process

```
URL validation (http/https only, localhost/private network blocked)

↓

Fetch with redirect validation, timeout, and response-size limit

↓

HTML main content extraction

↓

AI structured analysis

↓

JobPosting + JobPostingAnalysis snapshot persistence
```

### Response

```json
{
  "success": true,
  "code": "CREATED",
  "message": "...",
  "data": {
    "jobPostingId": 1,
    "jobPositionId": 1,
    "postingUrl": "https://careers.example.com/jobs/backend-developer",
    "title": "Backend Developer",
    "companyName": "Example Corp",
    "positionName": "Backend Developer",
    "responsibilities": ["..."],
    "requiredQualifications": ["..."],
    "preferredQualifications": [],
    "techStack": ["Java", "Spring Boot"],
    "experienceRequirements": [],
    "keywords": ["backend"],
    "summary": "...",
    "analyzedAt": "2026-08-26T15:00:00"
  }
}
```

---

Each request creates a new immutable snapshot because the source posting can change. A selected JobPosition is never
automatically changed from AI-extracted company or position information.

URL fetch failures return `JOB_POSTING_FETCH_FAILED`; inaccessible or empty posting content returns
`JOB_POSTING_CONTENT_NOT_FOUND`; disallowed URLs return `JOB_POSTING_URL_NOT_ALLOWED`.

---

# Interview API

## 면접 생성

### POST /api/v1/interviews

### Request

```json
{
  "jobPositionId": 1,
  "jobPostingId": 10,
  "title": "Backend Interview"
}
```

`jobPostingId` is optional. When omitted, the existing JobPosition-only question generation flow is used. When
provided, the posting must belong to `jobPositionId` and have a saved analysis.

### Process

```
Company + JobPosition + optional JobPostingAnalysis snapshot

Question distribution policy (difficulty and category)

↓

Prompt 생성

↓

Configured AI Provider

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

### POST /api/v1/interviews/{interviewId}/start

Only the owner can start an interview in `READY`. Questions are already generated when the interview is created.

### Response

```json
{
  "interviewId": 10,
  "status":"IN_PROGRESS",
  "startedAt":"2026-08-18T15:00:00"
}
```

### JobPosting validation errors

| Status | Code | Description |
|---|---|---|
| 404 | `JOB_POSTING_NOT_FOUND` | The requested JobPosting does not exist. |
| 409 | `JOB_POSTING_NOT_ANALYZED` | The JobPosting does not have a saved analysis. |
| 409 | `JOB_POSTING_POSITION_MISMATCH` | The JobPosting belongs to another JobPosition. |

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

### GET /api/v1/interviews

로그인 사용자의 면접 이력을 `createdAt` 내림차순으로 페이징 조회한다.

### Query Parameters

| 파라미터 | 기본값 | 설명 |
|---|---:|---|
| page | 0 | 페이지 번호 |
| size | 20 | 페이지 크기 |

### Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "content": [
      {
        "interviewId": 10,
        "title": "Backend Interview",
        "status": "COMPLETED",
        "createdAt": "2026-08-06T10:00:00",
        "startedAt": "2026-08-06T10:05:00",
        "completedAt": "2026-08-06T10:30:00",
        "cancelledAt": null,
        "companyName": "AI Interview",
        "positionName": "Backend Developer",
        "overallScore": 85,
        "feedbackExists": true,
        "partial": false
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## 면접 상세

### GET /api/v1/interviews/{interviewId}

The authenticated owner can retrieve interview state without changing it. Use `status` to choose Start, Progress, or Result navigation.

### Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "interviewId": 10,
    "title": "Backend Interview",
    "status": "IN_PROGRESS",
    "createdAt": "2026-08-18T14:00:00",
    "startedAt": "2026-08-18T14:05:00",
    "completedAt": null,
    "jobPositionId": 1,
    "positionName": "Backend Developer",
    "companyName": "AI Interview"
  }
}
```

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

## 질문별 답변 평가

### POST /api/v1/answers/{answerId}/evaluation

로그인 사용자는 본인 면접의 저장된 답변을 한 번만 AI 평가할 수 있다.

### Success Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "evaluationId": 1,
    "answerId": 10,
    "score": 85,
    "strengths": "핵심 개념을 정확히 설명했습니다.",
    "weaknesses": "구체적인 사례가 부족합니다.",
    "improvementSuggestion": "실제 적용 경험을 함께 설명해 보세요.",
    "reasoning": "질문의 핵심을 충족했으나 깊이가 일부 부족합니다."
  }
}
```

---

# Interview Answer Order Policy

`POST /api/v1/interviews/questions/{questionId}/answers` accepts only the first unanswered question in `questionOrder` sequence while the interview is `IN_PROGRESS`.

Requests for an answered question return `INTERVIEW_ANSWER_ALREADY_EXISTS`; requests out of order return `ANSWER_ORDER_INVALID`.

---

# AI Follow-up Question API

## POST /api/v1/ai/questions/{questionId}/follow-up

The authenticated interview owner can generate at most one follow-up question from a saved base-question answer. A follow-up question cannot generate another follow-up question. Progress exposes a generated follow-up immediately after its parent question.

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

# Interview Cancel API

### POST /api/v1/interviews/{interviewId}/cancel

Only the authenticated owner can cancel an `IN_PROGRESS` interview. A cancelled interview stores
`cancelledAt`; it does not set `completedAt` and cannot be started, completed, or cancelled again.

### Response

```json
{
  "interviewId": 10,
  "status": "CANCELLED",
  "cancelledAt": "2026-08-18T16:00:00"
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

Completed interview owners can generate one aggregate AI feedback result. A cancelled interview can generate
one partial feedback result only when it has at least two answers; unanswered questions are not sent to AI.
Partial feedback has `partial: true` and does not expose an `overallScore`.

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
    "partial": false,
    "answeredCount": 5,
    "totalQuestionCount": 5,
    "strengths": "...",
    "weaknesses": "...",
    "improvementSuggestions": "...",
    "summary": "..."
  }
}
```

---

## Interview Complete Response

### POST /api/v1/interviews/{interviewId}/complete

Only the owner can complete an interview in `IN_PROGRESS` after every question, including follow-up questions, has an answer.

### Success Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "interviewId": 10,
    "status": "COMPLETED",
    "completedAt": "2026-08-06T11:00:00"
  }
}
```

### Incomplete Response

```json
{
  "success": false,
  "code": "INTERVIEW_NOT_COMPLETABLE",
  "message": "All interview questions must be answered before completion.",
  "data": {
    "allAnswered": false,
    "unansweredCount": 2,
    "nextQuestionId": 4
  }
}
```

---

## Interview Progress Query

### GET /api/v1/interviews/{interviewId}/progress

Only the interview owner can retrieve an interview that is currently in progress.
Questions are returned in execution order: each base question is followed by its generated follow-up, when present.
`READY` returns `INTERVIEW_NOT_STARTED`; `COMPLETED` returns `INTERVIEW_ALREADY_COMPLETED`.

### Success Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "interviewId": 10,
    "status": "IN_PROGRESS",
    "questions": [
      {
        "questionId": 1,
        "parentQuestionId": null,
        "questionOrder": 1,
        "content": "...",
        "category": "TECH_STACK",
        "difficulty": "MEDIUM",
        "answerContent": "...",
        "answeredAt": "2026-08-06T10:00:00"
      }
    ],
    "nextQuestionId": 2,
    "allAnswered": false
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
    "completedAt": "2026-08-06T10:30:00",
    "companyName": "AI Interview",
    "positionName": "Backend Developer",
    "questionAnswers": [
      {
        "questionId": 1,
        "parentQuestionId": null,
        "questionOrder": 1,
        "questionContent": "...",
        "category": "TECH_STACK",
        "difficulty": "MEDIUM",
        "followUp": false,
        "answerContent": "...",
        "answeredAt": "2026-08-06T10:00:00",
        "evaluation": {
          "evaluationId": 1,
          "score": 85,
          "strengths": "...",
          "weaknesses": "...",
          "improvementSuggestion": "...",
          "reasoning": "..."
        }
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

## 면접 통계 요약

### GET /api/v1/dashboard/summary

로그인 사용자의 전체 면접 통계와 최근 5건의 면접 요약을 조회한다. Feedback이 없는 면접은 점수 통계에서 제외한다.

### Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "totalInterviews": 15,
    "completedInterviews": 8,
    "cancelledInterviews": 2,
    "averageScore": 82.5,
    "highestScore": 95,
    "latestInterviewAt": "2026-08-13T10:00:00",
    "recentInterviews": [
      {
        "interviewId": 10,
        "title": "Backend Interview",
        "status": "COMPLETED",
        "createdAt": "2026-08-13T10:00:00",
        "completedAt": "2026-08-13T10:30:00",
        "cancelledAt": null,
        "companyName": "AI Interview",
        "positionName": "Backend Developer",
        "overallScore": 85,
        "feedbackExists": true,
        "partial": false
      }
    ]
  }
}
```

---

## 점수 추이

### GET /dashboard/score-trend

로그인 사용자의 Feedback이 있는 완료 면접 중 최근 N건을 완료 시각 오름차순으로 조회한다.

### Query Parameters

| 파라미터 | 기본값 | 범위 | 설명 |
|---|---:|---:|---|
| limit | 10 | 1~100 | 최근 조회 건수 |

### Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": [
    {
      "interviewId": 8,
      "title": "Java Backend Interview",
      "completedAt": "2026-08-10T10:30:00",
      "overallScore": 80
    },
    {
      "interviewId": 10,
      "title": "Spring Interview",
      "completedAt": "2026-08-13T10:30:00",
      "overallScore": 85
    }
  ]
}
```

---

## 기간별 점수 분석

### GET /dashboard/analytics?period=WEEKLY

완료되고 Feedback이 생성된 본인 면접의 완료 시각을 주간 또는 월간으로 집계한다. `scoreChange`는 직전 반환 기간의 평균 점수와의 차이이며, 이전 데이터가 없으면 `null`이다.

### Query Parameters

| 파라미터 | 기본값 | 범위 | 설명 |
|---|---:|---:|---|
| period | WEEKLY | WEEKLY, MONTHLY | 집계 단위 |
| limit | 6 | 1~24 | 최근 집계 기간 수 |

### Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": [
    {
      "periodStartAt": "2026-08-03T00:00:00",
      "averageScore": 80.0,
      "interviewCount": 2,
      "scoreChange": null
    },
    {
      "periodStartAt": "2026-08-10T00:00:00",
      "averageScore": 85.0,
      "interviewCount": 3,
      "scoreChange": 5.0
    }
  ]
}
```

---

## 카테고리·난이도 약점 분석

### GET /api/v1/dashboard/weaknesses

완료 면접의 전체 질문을 카테고리·난이도별로 집계한다. `questionCount`는 해당 그룹의 전체 질문 수이고,
`evaluationCount`는 QuestionEvaluation이 존재하는 질문 수이다. 평가가 없는 질문도 표본 수에는 포함되지만,
`evaluationCount`가 0인 그룹은 평균 점수와 실제 약점 판단에서 제외한다. 가장 낮은 평균 점수를 약점으로 선택하며,
동점이면 평가 수가 많은 항목, 그마저 동점이면 enum 이름 오름차순을 적용한다.

### Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "performanceAnalysisAvailable": true,
    "unavailableReason": null,
    "weakestCategory": "CS",
    "weakestDifficulty": "HARD",
    "categoryStatistics": [
      {
        "category": "TECH_STACK",
        "interviewCount": 3,
        "questionCount": 8,
        "evaluationCount": 6,
        "averageScore": 72.5
      }
    ],
    "difficultyStatistics": [
      {
        "difficulty": "MEDIUM",
        "interviewCount": 3,
        "questionCount": 11,
        "evaluationCount": 9,
        "averageScore": 75.0
      }
    ]
  }
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
