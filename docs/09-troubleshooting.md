# 09. Troubleshooting

## Local PostgreSQL credentials

Local PostgreSQL uses separate administration and application roles.

- Administration role: `POSTGRES_ADMIN_USER` (default: `ai_interview_admin`)
- Application role: `DB_USERNAME` (default: `ai_interview_app`)
- Database: `POSTGRES_DB` (default: `ai_interview`)

Run `docker/setup-local-env.ps1` once to generate independent strong passwords in `docker/.env`. Alternatively, copy `docker/.env.example` to `docker/.env` and replace both placeholders manually. `docker/.env` is ignored by Git and must never be committed.

From Windows CMD at the project root, use the following commands:

```bat
powershell -NoProfile -ExecutionPolicy Bypass -File docker\setup-local-env.ps1
cd docker
docker compose up -d
cd ..
backend\run-local.cmd
```

The setup command is needed only when `docker/.env` does not exist. PostgreSQL is published only on `127.0.0.1:5432`; it must not be exposed on `0.0.0.0`.

Docker Compose reads `docker/.env` for container interpolation, but a Spring Boot JVM launched on Windows does not read that file automatically. `backend/run-local.cmd` bridges this boundary by loading only `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` into the Gradle child process. It does not expose the PostgreSQL administration password to Spring Boot, and `setlocal` prevents the database variables from remaining in the caller's CMD session after Backend exits.

The application configuration defaults `DB_URL` to `jdbc:postgresql://127.0.0.1:5432/ai_interview` and `DB_USERNAME` to `ai_interview_app`; `DB_PASSWORD` has no usable default and must come from the ignored local environment file. The explicit IPv4 address matches the Compose bind address and avoids ambiguity in Windows `localhost` resolution. Run `backend/run-local.cmd --args="--spring.profiles.active=..."` if application arguments are needed.

The application role is not a superuser and cannot create databases or roles. It receives only database connection and `public` schema usage/create privileges required by the current Hibernate `ddl-auto=update` development policy.

## Existing Docker data

The development volume has the explicit name `ai-interview-postgres-data`. `docker compose down` preserves it, while `docker compose down -v` deletes it and must not be used as a routine troubleshooting step.

PostgreSQL initialization scripts run only for an empty data directory. Changing environment variables does not update roles or passwords in an existing volume.
