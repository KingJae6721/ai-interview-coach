Project AI_Interview_Coach {
  database_type: 'PostgreSQL'
  Note: '''
    AI Interview Coach ERD v1.0
  '''
}

// =========================
// ENUM
// =========================

Enum UserRole {
  USER
  ADMIN
}

Enum InterviewStatus {
  READY
  IN_PROGRESS
  COMPLETED
  FAILED
}

Enum InterviewType {
  COMPANY
  CS
  PROJECT
  RANDOM
}

Enum Difficulty {
  EASY
  MEDIUM
  HARD
}

Enum QuestionCategory {
  CS
  TECH_STACK
  EXPERIENCE
  SITUATION
  COMPANY_FIT
}

Enum QuestionType {
  NORMAL
  FOLLOW_UP
}

// =========================
// USER
// =========================

Table users {
  id bigint [pk, increment]

  email varchar(255) [not null, unique]
  password varchar(255) [not null]
  nickname varchar(50)

  role UserRole [not null]

  created_at timestamp
  updated_at timestamp
}

// =========================
// COMPANY
// =========================

Table companies {
  id bigint [pk, increment]

  name varchar(100) [not null]
  normalized_name varchar(100) [not null, unique, note: 'NFKC, collapsed whitespace, lowercase']
  website_url varchar(255)
  logo_url varchar(255)

  created_at timestamp
  updated_at timestamp
}

// =========================
// JOB POSITION
// =========================

Table job_positions {
  id bigint [pk, increment]

  company_id bigint [not null, ref: > companies.id]

  name varchar(100) [not null]
  normalized_name varchar(100) [not null, note: 'NFKC, collapsed whitespace, lowercase; unique with company_id']

  tech_stack json

  interview_criteria text

  created_at timestamp
  updated_at timestamp
}

Indexes {
  (company_id, normalized_name) [unique, name: 'uk_job_positions_company_normalized_name']
}

`normalized_name`의 위 NOT NULL/UNIQUE 정의는 최종 목표 스키마다. 현재 프로젝트에는 Flyway/Liquibase가 없고
`ddl-auto=update`를 사용하므로, 배포 전에
`backend/src/main/resources/db/manual/V20260902__normalized_name_backfill.sql`을 운영자가 직접 실행해야 한다.
SQL은 정규화 충돌이나 기존 값 불일치를 발견하면 데이터를 병합하지 않고 전체 작업을 중단한다.

// =========================
// JOB POSTING
// =========================

Table job_postings {
  id bigint [pk, increment]

  job_position_id bigint [not null, ref: > job_positions.id]

  title varchar(255)

  posting_url text [not null]

  extracted_content text [not null]

  created_at timestamp
  updated_at timestamp
}

// =========================
// JOB POSTING ANALYSIS
// =========================

Table job_posting_analyses {
  id bigint [pk, increment]

  job_posting_id bigint [not null, unique, ref: > job_postings.id]

  company_name varchar(100)

  position_name varchar(100)

  responsibilities json

  required_qualifications json

  preferred_qualifications json

  tech_stack json

  experience_requirements json

  keywords json

  summary text

  ai_model varchar(100)

  analyzed_at timestamp [not null]

  created_at timestamp
  updated_at timestamp
}

// =========================
// RESUME
// =========================

Table resumes {
  id bigint [pk, increment]

  user_id bigint [not null, ref: > users.id]

  original_file_name varchar(255) [not null]

  file_size bigint [not null]

  content_type varchar(100) [not null]

  file_hash varchar(64) [not null]

  extracted_text text [not null]

  created_at timestamp
  updated_at timestamp
}

Table resume_analyses {
  id bigint [pk, increment]

  resume_id bigint [not null, unique, ref: > resumes.id]

  summary text
  skills json [not null]
  work_experiences json [not null]
  projects json [not null]
  education json [not null]
  certifications json [not null]
  achievements json [not null]
  strengths json [not null]
  keywords json [not null]
  ai_model varchar(100) [not null]
  analyzed_at timestamp [not null]

  created_at timestamp
  updated_at timestamp
}

// =========================
// INTERVIEW
// =========================

Table interviews {
  id bigint [pk, increment]

  user_id bigint [not null, ref: > users.id]

  job_position_id bigint [ref: > job_positions.id, note: 'Persisted derivation of JobPosting.jobPosition; nullable only for legacy rows']

  resume_id bigint [ref: > resumes.id, note: 'Optional analyzed resume snapshot used for question generation']

  job_posting_id bigint [ref: > job_postings.id, note: 'Required by the current create contract; nullable only for legacy rows']

  interview_type InterviewType

  difficulty Difficulty

  position varchar(100)

  status InterviewStatus

  current_question_order int

  started_at timestamp

  completed_at timestamp

  cancelled_at timestamp

  ended_at timestamp

  created_at timestamp
}

// =========================
// QUESTION
// =========================

Table questions {
  id bigint [pk, increment]

  interview_id bigint [not null, ref: > interviews.id]

  parent_question_id bigint [unique, ref: > questions.id]

  content text

  category QuestionCategory

  difficulty Difficulty

  type QuestionType

  sequence int // Base question sequence. Follow-up execution order is derived from parent_question_id.

  reason text

  created_at timestamp
}

// =========================
// ANSWER
// =========================

Table answers {
  id bigint [pk, increment]

  question_id bigint [not null, ref: > questions.id]

  content text

  duration int

  submitted_at timestamp

  created_at timestamp
}

// =========================
// FEEDBACK
// =========================

Table feedbacks {
  id bigint [pk, increment]

  interview_id bigint [not null, unique, ref: > interviews.id]

  overall_score int [not null]

  strengths text [not null]

  weaknesses text [not null]

  improvement_suggestions text [not null]

  summary text [not null]

  ai_model varchar(50) [not null]

  created_at timestamp

  updated_at timestamp
}

// =========================
// QUESTION EVALUATION
// =========================

Table question_evaluations {
  id bigint [pk, increment]

  answer_id bigint [not null, unique, ref: > answers.id]

  score int [not null]

  strengths text [not null]

  weaknesses text [not null]

  improvement_suggestion text [not null]

  reasoning text [not null]

  ai_model varchar(50) [not null]

  created_at timestamp
  updated_at timestamp
}
