# 07. Conventions

# 목적

프로젝트의 일관성을 유지하고 협업 효율을 높이기 위해 공통 규칙을 정의한다.

---

# Git Convention

## Branch Strategy

Git Flow를 간소화한 전략을 사용한다.

```
main
 └── develop
      ├── feature/auth
      ├── feature/interview
      ├── feature/resume
      ├── feature/job-posting
      ├── feature/ai
      └── feature/dashboard
```

### main

운영 브랜치

항상 배포 가능한 상태를 유지한다.

---

### develop

통합 개발 브랜치

모든 Feature Branch는 develop으로 Merge한다.

---

### feature/*

기능 개발 브랜치

예시

```
feature/auth

feature/interview

feature/resume

feature/feedback
```

---

# Commit Convention

Angular Commit Convention을 사용한다.

|Type|Description|
|------|----------------|
|feat|새로운 기능|
|fix|버그 수정|
|refactor|리팩토링|
|style|코드 스타일|
|docs|문서 수정|
|test|테스트|
|chore|설정 변경|

예시

```
feat: 회원가입 API 구현

fix: JWT 인증 오류 수정

docs: ERD 수정

refactor: InterviewService 분리
```

---

# Package Structure

```
com.aiinterview

├── global
│
├── auth
│
├── user
│
├── company
│
├── jobposting
│
├── resume
│
├── interview
│
├── question
│
├── answer
│
├── feedback
│
└── ai
```

Feature 기반 패키지 구조를 사용한다.

---

# Layer Structure

각 도메인은 동일한 Layer를 갖는다.

```
user

├── controller

├── service

├── repository

├── entity

├── dto

└── exception
```

---

# Entity Convention

## Entity 이름

단수 사용

```
User

Interview

Question
```

---

## Table 이름

복수 사용

```
users

interviews

questions
```

---

## Primary Key

```
id
```

Long 타입 사용

```
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

---

## 공통 컬럼

모든 Entity는 BaseEntity를 상속한다.

```
createdAt

updatedAt
```

BaseEntity

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
```

---

# DTO Convention

Request / Response 분리

```
UserSignupRequest

UserLoginRequest

InterviewCreateRequest

InterviewResponse
```

Entity를 직접 반환하지 않는다.

---

# API Convention

RESTful API 사용

예시

```
GET /users

GET /users/{id}

POST /users

PATCH /users/{id}

DELETE /users/{id}
```

---

# Response Convention

공통 Response 사용

```json
{
    "success": true,
    "data": {},
    "message": "Success"
}
```

---

# Validation

모든 Request는 Validation을 적용한다.

예시

```java
@NotBlank

@Email

@Size

@Pattern
```

Controller

```java
@Valid
```

사용

---

# Exception Convention

Global Exception Handler 사용

```
BusinessException

↓

GlobalExceptionHandler
```

도메인마다 Exception 정의

```
UserNotFoundException

InterviewNotFoundException

ResumeNotFoundException
```

---

# Logging

System.out.println 사용 금지

SLF4J 사용

```java
@Slf4j
```

예시

```java
log.info("Interview Created : {}", interviewId);
```

---

# Security

비밀번호

```
BCrypt
```

JWT 인증

Authorization Header

```
Bearer Token
```

---

# JPA Convention

기본 Fetch

```
LAZY
```

사용

N+1 발생 시

- Fetch Join
- EntityGraph

사용

Cascade는 필요한 경우만 사용

---

# QueryDSL

복잡한 검색은 QueryDSL 사용

단순 CRUD는 Spring Data JPA 사용

---

# Naming Convention

클래스

```
PascalCase
```

변수

```
camelCase
```

상수

```
UPPER_SNAKE_CASE
```

DB

```
snake_case
```

---

# Enum

문자열 저장

```
@Enumerated(EnumType.STRING)
```

사용

Ordinal 사용 금지

---

# 테스트

Service

```
JUnit5

Mockito
```

Repository

```
@DataJpaTest
```

Controller

```
@WebMvcTest
```

---

# Swagger

OpenAPI 3 사용

모든 API 문서화

```
/swagger-ui/index.html
```

---

# Code Review

PR 전 체크

- 빌드 성공
- 테스트 성공
- 불필요한 코드 제거
- 로그 제거
- 주석 확인

---

# AI Convention

OpenAI 호출은 반드시

```
AIService
```

를 통해 수행한다.

Controller에서 직접 호출 금지

Prompt는 Builder Pattern 사용

```
InterviewPromptBuilder

FeedbackPromptBuilder
```

사용

---

# 프로젝트 원칙

1. Entity를 API Response로 직접 반환하지 않는다.

2. Service는 비즈니스 로직만 담당한다.

3. Controller는 요청과 응답만 담당한다.

4. Repository는 DB 접근만 담당한다.

5. AI 관련 로직은 ai 패키지에서만 관리한다.

6. 공통 기능은 global 패키지에서 관리한다.

7. 모든 기능은 테스트 가능하도록 작성한다.

8. 코드보다 설계를 먼저 변경한다.