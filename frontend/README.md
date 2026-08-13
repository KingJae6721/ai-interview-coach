# AI Interview Coach Frontend

Next.js App Router 기반 프론트엔드입니다.

## 로컬 실행

Node.js 20.9 이상이 필요합니다.

```bash
npm install
copy .env.example .env.local
npm run dev
```

개발 서버는 기본적으로 `http://localhost:3000`, 백엔드는
`http://localhost:8081`을 사용합니다. 포트 3000이 사용 중이면
`npm run dev -- --port 3001`로 실행할 수 있습니다.

## 환경 변수

| 이름                       | 설명                     | 로컬 기본값             |
| -------------------------- | ------------------------ | ----------------------- |
| `NEXT_PUBLIC_API_BASE_URL` | Spring Boot API 기본 URL | `http://localhost:8081` |

`NEXT_PUBLIC_` 변수는 브라우저 번들에 공개되므로 비밀값을 저장하지 않습니다.

## 인증 및 CORS

- 현재 백엔드가 토큰을 JSON으로 반환하므로 Access/Refresh Token은 탭 종료 시 삭제되는 `sessionStorage`에 임시 저장합니다.
- 장기적으로 Refresh Token은 백엔드가 발급하는 `HttpOnly`, `Secure`, `SameSite` 쿠키로 전환하는 것이 안전합니다.
- API 클라이언트는 인증 요청에 `Authorization: Bearer {accessToken}`을 자동으로 추가합니다.
- 인증 요청이 401이면 `/api/v1/auth/reissue`를 한 번 호출하고 원 요청을 한 번만 재시도합니다.
- 동시에 발생한 여러 401은 하나의 재발급 Promise를 공유하며, 실패 시 세션을 제거하고 로그인 화면으로 이동합니다.
- `/dashboard`는 클라이언트 인증 가드로 보호되고, 로그인 사용자는 `/login`, `/signup`에서 `/dashboard`로 이동합니다.
- `sessionStorage`는 서버에서 읽을 수 없으므로 현재 가드는 화면 접근 제어이며, 실제 데이터 보안은 백엔드 JWT 검증이 담당합니다.
- 백엔드 Security 설정에 CORS 허용 정책이 없어 브라우저에서 `localhost:3000` → `localhost:8081` 요청이 차단될 수 있습니다.

## 명령어

```bash
npm run lint
npm run typecheck
npm run format:check
npm run build
```
