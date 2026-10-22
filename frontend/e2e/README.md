# Admin E2E

관리자 E2E는 production DB를 사용하지 않는다. Playwright가 `e2e/docker-compose.yml`의
격리된 PostgreSQL, Redis, Backend를 시작하고 Frontend production build를 대상으로 Chromium을 실행한다.

```bash
npm run test:e2e
```

최초 실행 전 Chromium 설치가 필요하다.

```bash
npx playwright install chromium
```

테스트용 credential은 E2E Compose 내부에서만 사용하는 고정 값이며 실제 환경에 사용하면 안 된다.
PostgreSQL은 tmpfs를 사용하고 global teardown이 `ai-interview-e2e` Compose project만 제거한다.
테스트가 강제 종료되어 컨테이너가 남은 경우 다음 명령으로 해당 project만 정리한다.

```bash
docker compose -p ai-interview-e2e -f e2e/docker-compose.yml down --volumes --remove-orphans
```
