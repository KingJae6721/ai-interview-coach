# Backend Development Skill

## Stack
- Java 17
- Spring Boot 4.1
- Spring Framework 7
- Hibernate 7
- PostgreSQL
- Gradle

## Rules
- 기존 Controller / Service / Repository 구조를 먼저 확인한다.
- API 응답은 기존 `ApiResponse<T>` 규칙을 따른다.
- 비즈니스 오류는 `BusinessException + ErrorCode`를 사용한다.
- Entity를 API Response로 직접 반환하지 않는다.
- 연관관계는 기본적으로 LAZY를 유지한다.
- N+1을 반드시 검토한다.
- 필요하면 EntityGraph/fetch join 등을 기존 패턴에 맞게 사용한다.
- 직접 EntityManager 접근은 특별한 이유가 없으면 피한다.
- AI/network 호출을 긴 DB transaction 안에서 수행하지 않는다.
- 동시성/unique constraint가 중요한 경우 `saveAndFlush()` 필요성을 검토한다.
- 기존 API 계약과 도메인 모델을 불필요하게 변경하지 않는다.

## Validation
- compileJava
- compileTestJava
- 관련 unit/integration test
- Testcontainers 실패 시 Docker 환경 실패와 코드 실패를 구분한다.