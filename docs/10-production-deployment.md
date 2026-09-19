# Production deployment: Vercel + Render + Supabase + Groq

## Architecture

`Browser -> Vercel (Next.js) -> Render (Spring Boot) -> Supabase PostgreSQL / managed Redis / Groq`

Supabase is used only as PostgreSQL. The frontend never connects to Supabase directly, and the existing JWT/sessionStorage API contract remains unchanged. Uploaded PDFs are parsed in memory; only metadata, the extracted text, and analysis are stored in PostgreSQL. No original file is retained on Render's filesystem.

## 1. Prepare Supabase PostgreSQL

1. Create the Supabase project.
2. In **Connect**, choose the **Shared Session Pooler** connection that is IPv4-compatible with Render. Do not use an IPv6-only direct host from Render.
3. Convert the supplied PostgreSQL values to a JDBC URL: `jdbc:postgresql://<host>:<port>/<database>?sslmode=require`.
4. Create a dedicated login role for the application. For the first deployment it must own, or have `USAGE, CREATE` on, its application schema because Flyway creates all application tables. Do not use the Supabase `postgres` administrator for normal application traffic. When using the shared pooler, copy the generated username; a custom role is represented as `<role>.<project-ref>`, not just `<role>`.
5. After migration, retain DML and sequence privileges plus schema usage; schema-change privileges remain necessary only for the identity used by Flyway during later deployments. If operational separation is required, run Flyway with a migration owner outside the application and grant the runtime role only `SELECT, INSERT, UPDATE, DELETE` on tables and `USAGE, SELECT, UPDATE` on sequences.

The application runs Flyway automatically before Hibernate validation. Production has `baseline-on-migrate=false`: a completely empty Supabase database receives `V1__initial_schema.sql` directly, followed by Hibernate schema validation. Do not enable baseline for this first production deployment.

### Existing non-Flyway database conversion

This is a separate maintenance procedure and must not be used for the empty production database. `baseline version 0` is unsafe: Flyway would record version `0` and then execute V1, whose `CREATE TABLE` statements conflict with the existing tables.

For a populated Hibernate-managed database:

1. Back up the database and stop application writes.
2. Compare its schema with `V1__initial_schema.sql`, including columns, types, nullability, foreign keys, unique/check constraints, and identity sequences. Apply reviewed corrective/manual migrations first if they differ.
3. Start the application once with `SPRING_FLYWAY_ENABLED=false`. The normal `ddl-auto=validate` must succeed; stop the application afterward. This validation does not replace the explicit constraint/index comparison in step 2.
4. Only after equivalence is confirmed, perform one controlled startup with `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` and `SPRING_FLYWAY_BASELINE_VERSION=1`. Because the schema is non-empty, Flyway records V1 as the baseline and does not execute the V1 `CREATE TABLE` statements.
5. Stop that instance, remove both temporary baseline variables, and restart normally with `baseline-on-migrate=false`.
6. Confirm `flyway_schema_history` contains a successful baseline at version `1`. Future migrations must start at V2 or later.

Never leave `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` in a persistent environment, and never use `docker compose down -v` as a migration procedure.

## 2. Prepare managed Redis

Create Render Key Value or another TLS-enabled managed Redis in the same region as the backend. Keep it private and supply its complete connection URI as `REDIS_URL`; do not expose port 6379 publicly. Redis is required for refresh-token storage and the logged-out access-token blacklist.

## 3. Deploy the backend to Render

The repository-root `render.yaml` deploys `backend/Dockerfile`. If configuring the service manually, use:

- Runtime: Docker
- Root directory: repository root
- Dockerfile: `backend/Dockerfile`
- Docker context: `backend`
- Health check path: `/actuator/health`

Set the environment variables listed below, deploy, and verify that `https://<render-domain>/actuator/health` returns `UP`. Render supplies `PORT`; Spring binds it automatically. The production profile disables Swagger UI and OpenAPI docs.

## 4. Deploy the frontend to Vercel

1. Import the repository and set **Root Directory** to `frontend`.
2. Keep the detected Next.js build command (`npm run build`).
3. Set `NEXT_PUBLIC_API_BASE_URL=https://<render-domain>` for Production before building. This value is public and is embedded at build time.
4. Deploy and copy the stable production origin, without a trailing slash.
5. Set the backend `CORS_ALLOWED_ORIGINS` to that origin and redeploy the backend.

Preview deployments are not allowed implicitly. If they must call production, append only specific trusted origins as a comma-separated value. Do not use `*`; a changing `*.vercel.app` preview policy should use a separate non-production backend instead.

## Environment variables

### Render backend

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_MAX_POOL_SIZE` (default `5`; size it within the Supabase connection limit)
- `REDIS_URL`
- `AI_PROVIDER=groq`
- `GROQ_API_KEY`
- `GROQ_MODEL` (optional; current default is used when omitted)
- `JWT_SECRET` (Base64-encoded, at least 32 random bytes before encoding)
- `JWT_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`
- `CORS_ALLOWED_ORIGINS`
- `SPRINGDOC_ENABLED=false`

`PORT` is provided by Render. Never put these secret values in Git, Render build logs, Vercel, or any `NEXT_PUBLIC_*` variable.

### Vercel frontend

- `NEXT_PUBLIC_API_BASE_URL`

## Production smoke test

After both deployments, use a disposable account and verify in order: signup, login, job-posting analysis, optional resume upload or selection, interview creation, start, answer, follow-up generation, next question, complete or cancel, evaluation, result view, and dashboard view. Confirm logout/reissue behavior against Redis as well.

Groq must be called with the configured production key. A missing key, provider quota, 429 exhaustion, or upstream outage is a failed/blocked smoke test—not a success. Do not log tokens, authorization headers, raw resume text, database credentials, or provider keys while diagnosing it.
