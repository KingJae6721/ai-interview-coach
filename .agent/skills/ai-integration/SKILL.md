# AI Integration Skill

## Architecture
기존 구조:
AiService → AiProvider → Provider implementation

이 추상화를 유지한다.

## Rules
- Provider와 model을 하드코딩하지 않는다.
- Groq/OpenAI/Gemini 등으로 교체 가능해야 한다.
- API Key를 frontend에 노출하지 않는다.
- Prompt는 전용 PromptBuilder/정책 클래스를 우선 사용한다.
- Structured Output을 우선한다.
- AI가 직접 계산하지 않아도 되는 deterministic logic은 Backend에서 계산한다.
- Prompt에는 필요한 최소 context만 전달한다.
- Resume/JobPosting/전체 대화를 무조건 넣지 않는다.
- AI 호출은 DB transaction 밖에서 수행한다.
- Provider 오류를 공통 오류 체계로 변환한다.
- 429/5xx 등 transient error와 4xx permanent error를 구분한다.
- token/context/rate-limit 비용을 고려한다.
- AI 실패 시 이미 저장된 성공 결과를 재사용할 수 있도록 idempotent하게 설계한다.

## Evaluation
- insufficient gate
- category rubric
- score anchor
- deterministic score aggregation
기존 정책을 임의로 약화하지 않는다.

## Future
TTS/STT도 같은 Provider abstraction이 필요할지 먼저 검토한다.
브라우저 Web API로 충분한 기능은 불필요하게 AI Provider로 보내지 않는다.