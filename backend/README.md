# 배달 시뮬레이터 백엔드

실시간 배달 매칭 시스템 시뮬레이터의 백엔드 서비스입니다.

## 기술 스택

- Kotlin 1.9.22
- Spring Boot 3.2.3
- Spring WebFlux (리액티브 웹 서버)
- Redis (캐시 데이터베이스)
- Kafka (이벤트 스트리밍)
- R2DBC MySQL (리액티브 데이터베이스 접근)

## 시작하기

### 환경 설정

1. 환경 변수 설정:
   - `.env.example` 파일을 `.env`로 복사
   - 민감 정보(DB, Redis, Kafka 연결 정보 등)를 실제 값으로 채워넣기

```bash
cp .env.example .env
# .env 파일 편집
```

2. 빌드 및 실행:

```bash
./gradlew bootRun
```

### 프로필 설정

- local: 로컬 개발 환경
- dev: 개발 서버 환경
- production: 프로덕션 환경

프로필에 따라 로깅 레벨만 다르게 설정됩니다. 모든 환경은 동일한 Redis, Kafka, DB를 사용합니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## API 엔드포인트

### 연결 테스트

- `GET /api/connection-test/status`: 모든 시스템 구성 요소의 연결 상태 확인
- `GET /api/connection-test/database`: 데이터베이스 연결 테스트
- `POST /api/connection-test/redis`: Redis 연결 테스트
- `POST /api/connection-test/kafka`: Kafka 연결 테스트
- `GET /api/connection-test/stream`: 리액티브 스트림 테스트

### WebSocket 엔드포인트

- `/ws/echo`: 에코 웹소켓 (클라이언트 메시지를 그대로 반환)
- `/ws/events`: 이벤트 스트림 웹소켓 (주기적으로 테스트 이벤트 발생)
