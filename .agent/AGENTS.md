# AGENTS.md

# AI Interview Coach

## Project Goal

AI 기반 면접 코칭 플랫폼

사용자가 원하는 기업과 직무를 선택하면 AI가 기업 맞춤 면접을 생성하고,
답변을 분석하여 피드백을 제공하는 서비스이다.

이 프로젝트는 학습용 CRUD 프로젝트가 아니라
실무 수준의 아키텍처와 유지보수를 고려한 포트폴리오 프로젝트이다.

---

# Tech Stack

## Backend

- Java 17+
- Spring Boot 4.x
- Spring Security
- Spring Data JPA
- QueryDSL
- Validation
- PostgreSQL

## Frontend

- Next.js
- TypeScript
- Tailwind CSS

## AI

- OpenAI API
- RAG (추후)
- Vector DB (추후)

## Infra

- Docker
- AWS
- GitHub Actions (추후)

---

# Architecture

Feature Based Architecture를 사용한다.

절대로 Layer Based Architecture로 변경하지 않는다.

패키지 구조

com.aiinterview

common
auth
user
company
job
interview
question
answer
feedback
ai

각 Feature는 아래 구조를 따른다.

feature

controller

service

repository

entity

dto

mapper

exception

---

# Layer Rule

Controller

- Request 수신
- Validation 수행
- Service 호출
- Response 반환

비즈니스 로직 작성 금지

---

Service

비즈니스 로직 담당

트랜잭션 처리

Repository 호출

다른 Service 호출 가능

Entity 생성

Entity 수정

---

Repository

JpaRepository 사용

복잡한 조회는 QueryDSL 사용

Native Query는 최소화

---

DTO

Request

Response

절대로 Entity를 API Response로 반환하지 않는다.

Entity ↔ DTO 변환은 Mapper에서 수행한다.

---

Entity

Entity는 DB와의 매핑만 담당한다.

Setter 사용 금지

생성자는 protected

생성은 static factory method 사용을 우선 고려한다.

---

# Entity Rules

모든 Entity는 BaseEntity를 상속한다.

BaseEntity

createdAt

updatedAt

JPA Auditing 사용

ID는 Long

PK 이름은

id

로 통일한다.

---

# Naming Convention

Class

PascalCase

Example

User

InterviewSession

CompanyPosition

---

Method

camelCase

---

Variable

camelCase

---

Constant

UPPER_SNAKE_CASE

---

Table

snake_case

Example

users

companies

interview_sessions

---

Column

snake_case

Example

created_at

updated_at

company_name

---

# JPA Rules

Lazy Loading 사용

OneToMany는 기본적으로 LAZY

ManyToOne도 필요하지 않으면 FetchType.LAZY

Cascade는 필요한 경우만 사용

orphanRemoval은 신중하게 사용

N+1 문제 발생 시

Fetch Join

EntityGraph

QueryDSL

중 하나를 사용한다.

무분별한 EAGER 사용 금지

---

# QueryDSL Rule

복잡한 조회는 QueryDSL

JpaRepository 메서드 이름이 길어지면 QueryDSL

동적 검색은 QueryDSL

---

# Transaction Rule

조회

@Transactional(readOnly = true)

수정

@Transactional

---

# Validation Rule

Bean Validation 사용

@NotBlank

@NotNull

@Email

@Size

Controller에서 @Valid 사용

---

# Exception Rule

예외는 GlobalExceptionHandler에서 처리

Controller에서 try-catch 금지

RuntimeException 직접 사용 금지

Custom Exception 생성

---

# Response Rule

모든 API는 ApiResponse<T> 사용

성공

{
  "success": true,
  "code": "SUCCESS",
  "message": "...",
  "data": {}
}

실패

{
  "success": false,
  "code": "...",
  "message": "...",
  "data": null
}

---

# Security Rule

JWT 기반 인증

Session 사용 금지

비밀번호는 BCrypt

Spring Security 사용

Role 기반 인가

ROLE_USER

ROLE_ADMIN

---

# Logging Rule

System.out.println 사용 금지

Slf4j 사용

중요 이벤트만 INFO

예외는 ERROR

---

# API Design

RESTful API

GET

POST

PUT

PATCH

DELETE

URL은 복수형

/users

/companies

/interviews

---

# Code Style

생성자 주입

@RequiredArgsConstructor

Field Injection 금지

Optional.get() 금지

Optional.orElseThrow() 사용

if depth 최소화

메서드는 하나의 책임만 가진다.

---

# Documentation

새로운 기능 추가 시

docs

ERD

API 문서

함께 수정한다.

---

# Git Convention

feat

fix

refactor

docs

test

style

chore

예시

feat: 회원가입 API 추가

fix: JWT 토큰 만료 오류 수정

refactor: UserService 분리

---

# Testing

Service는 단위 테스트 작성

Repository는 필요 시 DataJpaTest

MockMvc 사용

---

# AI Agent Rules

AI는 기존 구조를 임의로 변경하지 않는다.

패키지 구조를 임의 생성하지 않는다.

Entity를 Response로 반환하지 않는다.

Controller에 비즈니스 로직을 작성하지 않는다.

Setter를 생성하지 않는다.

무분별한 Lombok @Data 사용 금지

반드시

@Getter

@NoArgsConstructor(access = PROTECTED)

@RequiredArgsConstructor

를 우선 사용한다.

---

# Before Writing Code

항상 아래 순서를 따른다.

1. 요구사항 분석
2. 기존 구조 확인
3. 영향받는 클래스 확인
4. 필요한 파일 목록 제시
5. 코드 생성
6. 생성 이유 설명
7. 주의사항 설명

---

# When Generating Code

항상

- Best Practice
- SOLID
- Clean Code
- Spring Boot 공식 권장 방식
- JPA Best Practice

를 따른다.

단순히 동작하는 코드보다 유지보수 가능한 코드를 우선한다.