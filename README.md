# 🛒 NearSplit

**대용량 상품 공동구매 지역 기반 소분 플랫폼**

쿠팡, 코스트코 등에서 판매하는 대용량 상품을 근처 이웃과 함께 나눠 구매하는 서비스입니다.

---

## 📌 프로젝트 개요

- **서비스명**: NearSplit
- **핵심 가치**: 대용량 상품을 반경 3km 내 근처 사용자와 소분하여 경제적이고 친환경적인 구매 실현
- **주요 기능**:
  - 🏘️ 지역 기반 매칭 (반경 3km, PostGIS 활용)
  - 🛡️ 에스크로 결제 시스템
  - 🔗 외부 상품 API 연동 (쿠팡/코스트코)
  - 💬 실시간 채팅 (WebSocket)
  - 📊 신뢰도 평가 시스템

---

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.4.1
- **Language**: Java 17
- **Database**:
  - H2 (개발 환경)
  - PostgreSQL + PostGIS (프로덕션, 지리 정보 처리)
- **Authentication**: Spring Security + JWT (JJWT 0.12.3)
- **Real-time**: WebSocket
- **Caching**: Redis
- **Message Queue**: Kafka
- **API Documentation**: Swagger/OpenAPI 3.0

### Frontend (예정)
- React / Next.js
- TypeScript
- Tailwind CSS

### Infrastructure (예정)
- AWS
- Docker
- GitHub Actions (CI/CD)

---

## 🎯 개발 진행 상황

**✅ Week 1: 인증 및 사용자 관리** (완료)
- [x] Day 1-2: User 엔티티 및 Repository 작성 완료
- [x] Day 3-4: JWT 인증 및 AuthService 구현 완료
  - Spring Security 설정
  - JWT 토큰 생성/검증 (쿠키 기반)
  - 회원가입/로그인 API
- [x] Day 5-7: User API 완료
  - JWT 인증 필터 작성 (Authorization 헤더 + 쿠키 지원)
  - 프로필 조회/수정 API
  - 테스트 코드 작성 완료

**✅ Week 2: 소분 그룹 관리** (완료)
- [x] Day 1-3: SplitGroup & Participant 엔티티 및 Repository
- [x] Day 4-5: 소분 그룹 기본 API
  - 그룹 생성, 조회, 참여 신청, 승인, 거절
  - 내 그룹 목록 조회, 참여자 수 조회
- [x] Day 6-7: 추가 API
  - 전체 그룹 목록 조회 (페이징)
  - 그룹 수정 (PATCH), 삭제 (Soft Delete)
  - 참여 취소
  - 모집 완료 시 FULL 상태 자동 변경
  - 테스트 코드 작성 완료

**⬜ Week 3: 상품/채팅/알림** (예정)
**⬜ Week 4: 결제 및 거래** (예정)

---

## 🚀 빠른 시작

### 요구사항
- Java 17 이상
- Gradle 8.x

### 실행 방법

```bash
# 저장소 클론
git clone https://github.com/Jucy92/near_split.git
cd near_split/backend

# 빌드 및 실행
./gradlew bootRun

# 테스트 실행
./gradlew test
```

### API 문서 확인
서버 실행 후 Swagger UI 접속:
```
http://localhost:8080/swagger-ui.html
```

### H2 Console 접속 (개발 환경)
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (비어있음)
```

---

## 📁 프로젝트 구조

```
backend/
├── src/main/java/com/nearsplit/
│   ├── common/              # 공통 유틸리티 (JwtUtil 등)
│   ├── config/              # 설정 클래스 (SecurityConfig 등)
│   └── domain/
│       ├── user/            # 사용자 도메인
│       │   ├── entity/
│       │   ├── repository/
│       │   ├── service/
│       │   ├── controller/
│       │   └── dto/
│       ├── product/         # 상품 도메인 (예정)
│       ├── split/           # 소분 글 도메인 (예정)
│       └── chat/            # 채팅 도메인 (예정)
└── src/test/                # 테스트 코드
```

---

## 🔑 주요 API 엔드포인트

### 인증 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 쿠키 발급) |

### 사용자 API (예정)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/users/me` | 내 프로필 조회 |
| PATCH | `/api/users/me` | 내 프로필 수정 |

---

## 🧪 테스트

### 단위 테스트
```bash
./gradlew test
```

### curl 테스트 예시
```bash
# 회원가입
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123!",
    "name": "홍길동",
    "nickname": "gildong"
  }'

# 로그인 (쿠키에 JWT 저장)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123!"
  }' \
  -c cookies.txt

# 프로필 조회 (쿠키 사용)
curl -X GET http://localhost:8080/api/users/me -b cookies.txt
```

---

## 📚 문서

자세한 개발 가이드와 문서는 프로젝트 내부 문서를 참조하세요:
- 개발 가이드: `DEVELOPMENT_GUIDE.md` (비공개)
- ERD: `ERD.md` (비공개)
- API 명세: `API_SPEC.md` (비공개)

---

## 📞 라이선스 및 문의

**프로젝트 상태**: 🟢 개발 진행 중
**마지막 업데이트**: 2025-12-09
**개발 기간**: Week 1 진행 중 (총 8주 예정)
