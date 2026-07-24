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

### POST /auth/signup

### Request

```json
{
  "email": "test@test.com",
  "password": "1234",
  "nickname": "kim"
}
```

### Response

```json
{
  "userId": 1,
  "email": "test@test.com"
}
```

### Validation

- 이메일 중복 불가
- 비밀번호 8자 이상

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
  "resumeId":1,
  "jobPostingId":3,
  "difficulty":"MEDIUM",
  "interviewType":"COMPANY"
}
```

### Process

```
Resume

+

JobPosting

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

### POST /ai/follow-up

### Request

```json
{
  "questionId":1,
  "answer":"..."
}
```

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