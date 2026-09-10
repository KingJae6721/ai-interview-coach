# Database Migration Skill

## Rules
- Hibernate schema 자동 변경만 믿지 않는다.
- production schema 변경은 명시적인 migration 전략을 우선한다.
- 현재 프로젝트에는 versioned migration tool이 없으므로 기존 manual migration 정책을 확인한다.
- destructive migration 전 데이터 영향도를 확인한다.
- PostgreSQL constraint/index/NOT NULL/unique 변경 전 collision을 검사한다.
- backfill → validation → constraint 순서를 지킨다.
- SQL은 가능한 한 재실행 안전성 또는 명확한 실패 조건을 가진다.
- Docker volume 삭제를 migration 해결책으로 사용하지 않는다.
- `docker compose down -v`는 데이터 삭제 작업이므로 사용자 승인 없이 권장 실행하지 않는다.

## Local DB
Compose 작업은 `docker/` 디렉터리 기준으로 수행한다.

PostgreSQL:
- db: ai_interview
- user: postgres
- password: postgres