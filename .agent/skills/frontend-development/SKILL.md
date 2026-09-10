# Frontend Development Skill

## Stack
- Next.js 16 App Router
- TypeScript
- Tailwind CSS 4
- native fetch

## Rules
- 기존 component/hook/lib 구조를 먼저 확인한다.
- Backend API 계약을 실제 코드/DTO 기준으로 확인한다.
- 존재하지 않는 응답 필드를 추측해서 만들지 않는다.
- access/refresh token은 기존 인증 흐름을 재사용한다.
- 401 refresh/retry 로직을 중복 구현하지 않는다.
- loading/error/empty 상태를 처리한다.
- mobile/desktop responsive를 확인한다.
- 접근성을 고려한다.
- SSR에서 `window`, `document`, browser API에 직접 접근하지 않는다.
- 임의의 mock 데이터로 성공 처리하지 않는다.

## Validation
- Prettier
- tsc --noEmit
- ESLint
- Next production build