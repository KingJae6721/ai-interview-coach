# Flyway legacy database migration

이 문서는 Flyway 도입 전에 Hibernate가 생성한 기존 PostgreSQL 데이터베이스를 현재의
`Flyway + Hibernate ddl-auto=validate` 정책으로 전환하는 운영 절차다. 신규 데이터베이스에는
이 절차를 사용하지 않는다.

## 1. 신규 데이터베이스

신규 빈 데이터베이스는 애플리케이션의 기본 설정으로 시작한다.

```text
empty database
→ V1__initial_schema.sql
→ V2__create_admin_audit_logs.sql
→ Hibernate validate
```

`baseline-on-migrate`는 기본값 `false`를 유지한다.

## 2. 전환 전 필수 조건

1. DB backup을 생성하고 복원 가능 여부를 확인한다.
2. 애플리케이션 쓰기를 중지한다.
3. 실제 운영 DB가 아닌 dump/clone DB에서 아래 절차를 먼저 검증한다.
4. 기존 schema가 V1과 의미상 동등한지 확인한다.

비교 범위:

- table, column, PostgreSQL data type과 길이
- nullability와 identity/sequence
- PK, FK, unique, check constraint
- 일반 index와 constraint index
- default expression

현재 로컬 legacy DB 검증에서는 컬럼·타입·NULL·PK/FK·unique/check·identity sequence가
V1과 의미상 일치했다. Hibernate가 생성한 일부 FK/unique constraint 이름은 V1의 이름과
다르지만 제약 정의는 동일하고 V2는 해당 이름에 의존하지 않는다.

제약 이름이 다르다는 이유만으로 기존 제약을 삭제하거나 재생성하지 않는다. 이후 migration이
특정 제약 이름에 의존하게 된다면 해당 환경의 실제 이름을 먼저 검사하는 별도 migration이 필요하다.

## 3. Schema 비교용 metadata query

다음 query 결과를 V1만 적용한 임시 DB 결과와 비교한다. constraint 이름이 아니라 정의를
비교하는 것이 중요하다.

```sql
select 'C|' || c.relname || '|' || a.attname || '|'
       || format_type(a.atttypid, a.atttypmod) || '|'
       || a.attnotnull::text || '|' || a.attidentity::text
from pg_attribute a
join pg_class c on c.oid = a.attrelid
join pg_namespace n on n.oid = c.relnamespace
where n.nspname = 'public'
  and c.relkind = 'r'
  and a.attnum > 0
  and not a.attisdropped
union all
select 'K|' || c.relname || '|' || con.contype::text || '|'
       || pg_get_constraintdef(con.oid, true)
from pg_constraint con
join pg_class c on c.oid = con.conrelid
join pg_namespace n on n.oid = c.relnamespace
where n.nspname = 'public'
union all
select 'S|' || sequencename || '|' || data_type || '|'
       || start_value || '|' || increment_by
from pg_sequences
where schemaname = 'public'
order by 1;
```

차이가 있으면 baseline을 진행하지 않는다. 먼저 검토된 보정 migration 또는 수동 변경 계획을
작성하고 clone DB에서 다시 검증한다.

## 4. 기존 DB 전환 절차

아래 환경변수는 clone 검증과 실제 maintenance window에서 한 번만 사용한다.

```text
SPRING_FLYWAY_BASELINE_ON_MIGRATE=true
SPRING_FLYWAY_BASELINE_VERSION=1
```

절차:

1. backup과 write 중지를 재확인한다.
2. V1 schema 동등성 검사를 통과했는지 확인한다.
3. 위 두 환경변수를 설정하고 애플리케이션을 한 번 시작한다.
4. Flyway가 기존 V1 schema를 실행하지 않고 version 1을 `BASELINE`으로 기록한다.
5. V2 이상 migration이 실행되고 Hibernate validate가 통과해야 애플리케이션이 시작된다.
6. 애플리케이션을 중지한다.
7. 두 임시 환경변수를 완전히 제거한다.
8. 기본 설정(`baseline-on-migrate=false`)으로 다시 시작한다.
9. 기존 핵심 데이터의 row count와 업무 표본을 backup 전 값과 비교한다.

현재 Entity에는 V2의 `admin_audit_logs`가 포함되므로 V2 적용 전에 현재 애플리케이션의
Hibernate validate만 단독 실행하면 실패하는 것이 정상이다. V1 동등성은 metadata 비교로
검증하고, Hibernate validate는 baseline과 V2가 완료된 최종 schema에서 수행한다.

## 5. 완료 확인

```sql
select installed_rank, version, description, type, success
from flyway_schema_history
order by installed_rank;
```

기대 결과:

```text
1 | 1 | << Flyway Baseline >>       | BASELINE | true
2 | 2 | create admin audit logs     | SQL      | true
```

추가 확인:

```sql
select to_regclass('public.admin_audit_logs');
```

정상 재시작 로그에서 Flyway validation과 Hibernate schema validation 성공도 확인한다.

## 6. 금지 사항

- `baseline version 0`: V1의 `CREATE TABLE`이 기존 테이블과 충돌한다.
- V1/V2 파일 수정: 이미 적용된 migration checksum과 환경 간 일관성을 훼손한다.
- `ddl-auto=update`: 실제 차이를 숨기고 운영 schema를 암묵적으로 변경한다.
- `docker compose down -v`: migration 대신 데이터를 삭제하는 행위다.
- schema 동등성 확인 없는 baseline
- baseline 환경변수를 상시 배포 설정에 남기는 행위
- 실패한 history row를 임의로 수정하거나 삭제하는 행위

## 7. 실패와 rollback

1. migration 또는 Hibernate validate가 실패하면 write를 재개하지 않는다.
2. 애플리케이션을 중지하고 로그와 `flyway_schema_history`를 보존한다.
3. 실패 원인을 별도 clone에서 재현한다.
4. 실제 DB는 검증된 backup을 새로운 DB에 복원하여 되돌린다.
5. 복원 DB의 row count와 주요 관계를 검증한 뒤 연결 대상을 전환한다.

부분 적용된 schema를 역방향 SQL로 즉석 복구하거나 schema history만 삭제해서 재시도하지 않는다.
실제 credential, dump 파일, 개인정보는 Git과 문서에 기록하지 않는다.
