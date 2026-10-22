import { request } from "@playwright/test";
import { Client } from "pg";

import { E2E } from "./fixtures";

async function signup(email: string, nickname: string): Promise<void> {
  const api = await request.newContext({ baseURL: E2E.backendBaseUrl });
  try {
    const response = await api.post("/api/v1/auth/signup", {
      data: { email, password: E2E.password, nickname },
    });
    if (response.status() !== 201) {
      throw new Error(
        `E2E signup failed for ${email}: HTTP ${response.status()}`,
      );
    }
  } finally {
    await api.dispose();
  }
}

export default async function globalSetup(): Promise<void> {
  await signup(E2E.adminEmail, "e2e-admin");
  await signup(E2E.userEmail, "e2e-user");

  const client = new Client({
    host: "127.0.0.1",
    port: 55432,
    database: "ai_interview_e2e",
    user: "e2e_admin",
    password: "e2e-admin-password-not-for-production",
  });

  await client.connect();
  try {
    await client.query("begin");
    await client.query("update users set role = 'ADMIN' where email = $1", [
      E2E.adminEmail,
    ]);
    await client.query(`
      insert into users (
        created_at, updated_at, auth_provider, email, nickname,
        password, role, status
      )
      select now(), now(), 'LOCAL',
             'member' || lpad(value::text, 2, '0') || '@example.test',
             'member-' || value, 'not-used-by-e2e', 'USER', 'ACTIVE'
      from generate_series(1, 23) value
    `);

    const ownerResult = await client.query<{ id: number }>(
      "select id from users where email = $1",
      [E2E.userEmail],
    );
    const ownerId = ownerResult.rows[0]?.id;
    if (!ownerId) throw new Error("E2E owner user was not created");

    const companyResult = await client.query<{ id: number }>(`
      insert into companies (
        created_at, updated_at, name, normalized_name, website_url, logo_url
      ) values (now(), now(), 'E2E Company', 'e2e company', null, null)
      returning id
    `);
    const companyId = companyResult.rows[0]?.id;

    const positionResult = await client.query<{ id: number }>(
      `insert into job_positions (
         created_at, updated_at, company_id, name, normalized_name,
         tech_stack, interview_criteria
       ) values (now(), now(), $1, 'Backend Developer', 'backend developer',
                 '["Java", "Spring Boot"]'::jsonb, 'problem solving')
       returning id`,
      [companyId],
    );
    const positionId = positionResult.rows[0]?.id;

    const postingResult = await client.query<{ id: number }>(
      `insert into job_postings (
         created_at, updated_at, user_id, job_position_id,
         posting_url, title, extracted_content
       ) values (now(), now(), $1, $2,
                 'https://example.test/jobs/backend', 'E2E Backend Posting',
                 'Backend role fixture') returning id`,
      [ownerId, positionId],
    );
    const postingId = postingResult.rows[0]?.id;

    await client.query(
      `insert into job_posting_analyses (
         created_at, updated_at, job_posting_id, company_name, position_name,
         responsibilities, required_qualifications, preferred_qualifications,
         tech_stack, experience_requirements, keywords, summary, analyzed_at, ai_model
       ) values (now(), now(), $1, 'E2E Company', 'Backend Developer',
                 '["API development"]'::jsonb, '["Java"]'::jsonb,
                 '["Spring Boot"]'::jsonb, '["Java", "Spring Boot"]'::jsonb,
                 '[]'::jsonb, '["backend"]'::jsonb,
                 'E2E posting summary', now(), 'e2e')`,
      [postingId],
    );

    const resumeResult = await client.query<{ id: number }>(
      `insert into resumes (
         created_at, updated_at, user_id, original_file_name,
         file_size, content_type, extracted_text, file_hash
       ) values (now(), now(), $1, 'e2e-resume.pdf', 100,
                 'application/pdf', $2, repeat('a', 64)) returning id`,
      [ownerId, E2E.resumeSentinel],
    );
    const resumeId = resumeResult.rows[0]?.id;

    const interviewResult = await client.query<{ id: number }>(
      `insert into interviews (
         created_at, updated_at, user_id, job_position_id, job_posting_id,
         resume_id, title, status
       ) values (now(), now(), $1, $2, $3, $4, $5, 'READY') returning id`,
      [ownerId, positionId, postingId, resumeId, E2E.interviewTitle],
    );
    const interviewId = interviewResult.rows[0]?.id;

    const questionResult = await client.query<{ id: number }>(
      `insert into interview_questions (
         created_at, updated_at, interview_id, content, question_order,
         is_ai_generated, category, difficulty, type
       ) values (now(), now(), $1, 'Explain transaction isolation.', 1,
                 true, 'CS', 'MEDIUM', 'NORMAL') returning id`,
      [interviewId],
    );
    const questionId = questionResult.rows[0]?.id;

    const answerResult = await client.query<{ id: number }>(
      `insert into interview_answers (
         created_at, updated_at, question_id, answer_content, answered_at
       ) values (now(), now(), $1, $2, now()) returning id`,
      [questionId, E2E.answerSentinel],
    );
    await client.query(
      `insert into question_evaluations (
         created_at, updated_at, answer_id, score, strengths, weaknesses,
         improvement_suggestion, reasoning, ai_model
       ) values (now(), now(), $1, 80, 'clear', 'detail', 'add example',
                 'fixture evaluation', 'e2e')`,
      [answerResult.rows[0]?.id],
    );

    await client.query(
      `insert into interviews (
         created_at, updated_at, user_id, job_position_id, job_posting_id,
         title, status, started_at
       ) values
         (now() - interval '1 day', now(), $1, $2, $3, 'E2E In Progress', 'IN_PROGRESS', now()),
         (now() - interval '2 days', now(), $1, $2, $3, 'E2E Completed', 'COMPLETED', now()),
         (now() - interval '3 days', now(), $1, $2, $3, 'E2E Cancelled', 'CANCELLED', now())`,
      [ownerId, positionId, postingId],
    );

    const auditActors = await client.query<{ id: number }>(
      "select id from users where email like 'member%@example.test' order by id limit 1",
    );
    await client.query(
      `insert into admin_audit_logs (
         created_at, updated_at, actor_user_id, target_type, target_id,
         action, before_value, after_value
       )
       select now() - (value || ' minutes')::interval, now(), $1, 'USER', $2,
              'USER_ROLE_CHANGED', 'ADMIN', 'USER'
       from generate_series(1, 21) value`,
      [auditActors.rows[0]?.id, ownerId],
    );
    await client.query("commit");
  } catch (error) {
    await client.query("rollback");
    throw error;
  } finally {
    await client.end();
  }
}
