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

## 명령어

```bash
npm run lint
npm run typecheck
npm run format:check
npm run build
```
