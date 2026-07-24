# 06. Architecture

## Overall Architecture

```text
                  +----------------------+
                  |      Browser         |
                  +----------+-----------+
                             |
                             |
                             ▼
                  +----------------------+
                  |     Next.js App      |
                  |  (Frontend, Vercel)  |
                  +----------+-----------+
                             |
                   HTTPS / REST API
                             |
                             ▼
                 +------------------------+
                 |   Spring Boot API      |
                 |   (Backend, EC2)       |
                 +----+-----------+-------+
                      |           |
             JPA      |           | OpenAI API
                      |           |
                      ▼           ▼
          +----------------+   +------------------+
          | PostgreSQL(RDS)|   | OpenAI Platform  |
          +----------------+   +------------------+
                  |
                  |
                  ▼
             pgvector

                  |

                  ▼

            Resume Embedding


                  ▲

                  |

          +---------------+

          | AWS S3 |

          +---------------+

              Resume PDF
```

---

# Technology Stack

| Layer | Technology |
|--------|------------|
|Frontend|Next.js + TypeScript|
|Backend|Spring Boot + Java17|
|Security|Spring Security + JWT|
|Database|PostgreSQL|
|ORM|JPA + QueryDSL|
|Cache|Redis|
|AI|OpenAI API|
|Embedding|pgvector|
|Storage|AWS S3|
|Deployment|Docker + AWS|

---

# Frontend

## 역할

- 로그인
- 면접 생성
- 질문 표시
- 답변 입력
- 결과 화면
- 통계 화면

---

# Backend

## 역할

- JWT 인증

- API 제공

- DB 저장

- OpenAI 호출

- RAG 검색

- AI Prompt 생성

---

# AI Flow

```text
Resume

+

JobPosting

↓

Prompt Builder

↓

OpenAI

↓

Question 생성

↓

Question 저장
```

---

# Feedback Flow

```text
Question

↓

Answer

↓

Prompt Builder

↓

OpenAI

↓

Feedback

↓

DB 저장
```

---

# Resume Flow

```text
PDF Upload

↓

AWS S3

↓

Text Parsing

↓

Embedding

↓

pgvector 저장
```

---

# Job Posting Flow

```text
URL 입력

↓

본문 수집

↓

텍스트 정제

↓

OpenAI 분석

↓

기술 스택 추출

↓

DB 저장
```

---

# Authentication

```text
회원가입

↓

BCrypt

↓

Database

↓

로그인

↓

JWT 발급

↓

Frontend 저장

↓

Authorization Header
```

---

# Deployment

```text
GitHub

↓

GitHub Actions

↓

Docker Build

↓

AWS EC2

↓

Spring Boot
```

Frontend

```text
GitHub

↓

Vercel

↓

Automatic Deploy
```

---

# RAG Flow

```text
Resume

↓

Embedding

↓

Vector DB

↓

Similarity Search

↓

Prompt

↓

GPT

↓

질문 생성
```

---

# Folder Structure

Backend

```text
backend

├── auth

├── user

├── company

├── jobposting

├── resume

├── interview

├── question

├── answer

├── feedback

├── ai

├── common
```

Frontend

```text
frontend

├── app

├── components

├── features

├── hooks

├── services

├── stores

├── types
```

---

# External Services

|Service|Purpose|
|--------|-------|
|OpenAI|질문 생성|
|AWS S3|파일 저장|
|Redis|Refresh Token|
|PostgreSQL|데이터 저장|
|pgvector|Embedding 검색|

---

# Future Architecture

향후

- GitHub Repository 분석
- 음성 면접(STT)
- WebRTC
- AI Avatar

추가를 고려하여 모듈화를 유지한다.