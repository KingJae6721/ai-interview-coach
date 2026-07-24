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
  TECH
  CS
  PROJECT
  PERSONALITY
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
  homepage varchar(255)
  logo_url varchar(255)

  created_at timestamp
}

// =========================
// JOB POSTING
// =========================

Table job_postings {
  id bigint [pk, increment]

  company_id bigint [not null, ref: > companies.id]

  title varchar(255)

  position varchar(100)

  url text

  description text

  requirements text

  preferred_qualifications text

  tech_stack json

  created_at timestamp
}

// =========================
// RESUME
// =========================

Table resumes {
  id bigint [pk, increment]

  user_id bigint [not null, ref: > users.id]

  original_file_name varchar(255)

  file_url text

  parsed_text text

  created_at timestamp
}

// =========================
// INTERVIEW
// =========================

Table interviews {
  id bigint [pk, increment]

  user_id bigint [not null, ref: > users.id]

  resume_id bigint [ref: > resumes.id]

  job_posting_id bigint [ref: > job_postings.id]

  interview_type InterviewType

  difficulty Difficulty

  position varchar(100)

  status InterviewStatus

  current_question_order int

  started_at timestamp

  ended_at timestamp

  created_at timestamp
}

// =========================
// QUESTION
// =========================

Table questions {
  id bigint [pk, increment]

  interview_id bigint [not null, ref: > interviews.id]

  content text

  category QuestionCategory

  type QuestionType

  sequence int

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

  answer_id bigint [not null, ref: > answers.id]

  total_score int

  technical_score int

  logic_score int

  communication_score int

  specificity_score int

  strength text

  weakness text

  recommendation text

  ai_model varchar(50)

  created_at timestamp
}