# ai-interview-coach
AI를 활용한 면접 코칭 프로젝트입니다.

Production deployment instructions: [docs/10-production-deployment.md](docs/10-production-deployment.md)
# 최초 관리자 지정

관리자 회원가입 API는 제공하지 않는다. 최초 관리자는 일반 회원가입 후 운영자가 DB 관리 권한으로 다음 SQL을 실행해 한 명만 승격한다.

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = '<ADMIN_EMAIL>';
```

`<ADMIN_EMAIL>`에는 실제 가입된 관리자 이메일을 입력하며, 실제 이메일이나 운영 credential을 Git tracked 파일에 기록하지 않는다. 최초 승격 이후의 권한 변경은 `PATCH /api/v1/admin/users/{userId}/role`을 사용한다.
