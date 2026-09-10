# Project Quality Skill

작업 완료 전에 반드시 확인한다.

## Review
- 기존 변경사항을 보존했는가
- 불필요한 리팩터링이 없는가
- 불필요한 새 파일/의존성이 없는가
- API 계약을 깨지 않았는가
- N+1/transaction/concurrency 문제가 없는가
- frontend/backend 책임이 적절히 분리됐는가

## Tests
변경 범위에 맞는 테스트를 실행한다.

환경 문제로 테스트가 실패했다면:
- 코드 실패
- Docker/Testcontainers 실패
- 브라우저 환경 부재
를 구분해서 보고한다.

수행하지 않은 테스트를 성공했다고 보고하지 않는다.

## Docs
API나 동작 계약이 바뀌었다면 기존 docs를 갱신한다.

## Final Report
다음 형식으로 간단히 보고한다.

1. 원인
2. 변경 내용
3. 변경 파일
4. 테스트 결과
5. 환경상 미검증 항목
6. 남은 문제
7. 다음 Sprint