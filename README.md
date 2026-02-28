# 🛒 NearSplit

**대용량 상품 공동구매 지역 기반 소분 플랫폼**

쿠팡, 코스트코 등에서 판매하는 대용량 상품을 구매하여 근처 이웃과 함께 나누는 서비스입니다.

---

## 📌 프로젝트 개요

- **서비스명**: NearSplit
- **핵심 가치**: 대용량 상품을 근처 사용자와 소분하여 경제적이고 친환경적인 구매 실현
- **주요 기능**:
  - 🏘️ 지역 기반 소분 그룹 매칭
  - 🛒 상품 등록 및 외부 상품 연동
  - 💬 실시간 채팅 (WebSocket/STOMP)
  - 🔔 실시간 알림 (WebSocket)
  - 💳 토스페이먼츠 결제 연동
  - 🗺️ 도로명주소 검색 및 좌표 변환 (Juso API, VWorld API)

---

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.4.0
- **Language**: Java 17
- **Database**: H2 (개발 환경)
- **Authentication**: Spring Security + JWT (JJWT 0.12.x)
- **Real-time**: WebSocket + STOMP
- **ORM**: Spring Data JPA + QueryDSL
- **Payment**: 토스페이먼츠 (Toss Payments)

### Frontend
- **Framework**: Vue.js 3
- **Build Tool**: Vite
- **HTTP Client**: Axios
- **Real-time**: STOMP over WebSocket

### Infrastructure
- **Deployment**: Railway (백엔드), Vercel (프론트엔드)

---

## 🎯 개발 진행 상황

**✅ 인증 및 사용자 관리** (완료)
- [x] User 엔티티 및 Repository
- [x] JWT 인증 (Spring Security, JJWT 0.12.x)
  - 회원가입 / 로그인 API (쿠키 기반)
  - JWT 인증 필터 (Authorization 헤더 + 쿠키 지원)
  - 토큰 갱신 / 로그아웃
- [x] 프로필 조회 / 수정 API
- [x] 테스트 코드 작성 완료

**✅ 소분 그룹 관리** (완료)
- [x] SplitGroup & Participant 엔티티 및 Repository (QueryDSL 포함)
- [x] 그룹 생성 / 조회 / 수정 / 삭제 (Soft Delete)
- [x] 참여 신청 / 취소 / 승인 / 거절
- [x] 모집 완료 시 FULL 상태 자동 전환
- [x] 분담금 자동 계산 (총 금액 / (최대 인원 + 1))
- [x] 테스트 코드 작성 완료

**✅ 상품 관리** (완료)
- [x] Product 엔티티 및 Repository
- [x] 상품 등록 / 조회 / 검색 / 수정 / 삭제
- [x] 외부 상품 연동 지원 (externalId, externalSource)

**✅ 채팅 시스템** (완료)
- [x] ChatMessage 엔티티 및 Repository
- [x] WebSocket + STOMP 실시간 채팅 (`/app/chat/{groupId}/send`)
- [x] 채팅 히스토리 조회 (페이징) / 최근 메시지 50개 조회
- [x] 참여자 검증 (비참여자 메시지 수신 차단)

**✅ 알림 시스템** (완료)
- [x] Notification 엔티티 및 Repository
- [x] 참여 신청 / 승인 / 거절 / 모집 완료 알림
- [x] WebSocket 실시간 알림 발송
- [x] 알림 목록 조회 / 읽음 처리 / 전체 읽음

**✅ 결제 시스템** (완료)
- [x] Payment 엔티티 및 Repository
- [x] 토스페이먼츠 결제 승인 / 조회 / 취소
- [x] 결제 후 Participant 상태 PAID 자동 전환
- [x] 결제 내역 조회

**✅ 외부 API 연동** (완료)
- [x] Juso API - 도로명주소 검색 (`GET /api/address/search`)
- [x] VWorld API - 주소 → 좌표 변환 (지오코딩)
- [x] Toss Payments API - 결제 처리

---

## 🔑 주요 API 엔드포인트

### 인증 (`/api/auth`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 쿠키 발급) |
| POST | `/api/auth/logout` | 로그아웃 |
| POST | `/api/auth/refresh` | 토큰 갱신 |

### 사용자 (`/api/users`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/users/me` | 내 프로필 조회 |
| PATCH | `/api/users/me` | 내 프로필 수정 |

### 소분 그룹 (`/api/split`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/split` | 전체 그룹 목록 (페이징) |
| POST | `/api/split` | 그룹 생성 |
| GET | `/api/split/my` | 내 그룹 목록 |
| GET | `/api/split/{groupId}` | 그룹 상세 |
| PATCH | `/api/split/{groupId}` | 그룹 수정 |
| DELETE | `/api/split/{groupId}` | 그룹 삭제 |
| POST | `/api/split/{groupId}/join` | 참여 신청 |
| DELETE | `/api/split/{groupId}/join` | 참여 취소 |
| POST | `/api/split/{groupId}/approve` | 참여자 승인 |
| POST | `/api/split/{groupId}/reject` | 참여자 거절 |

### 상품 (`/api/products`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/products` | 상품 목록 (페이징) |
| POST | `/api/products` | 상품 등록 |
| GET | `/api/products/{id}` | 상품 상세 |
| GET | `/api/products/search` | 상품 검색 |
| PATCH | `/api/products/{id}` | 상품 수정 |
| DELETE | `/api/products/{id}` | 상품 삭제 |

### 채팅 (`/api/chat`, WebSocket)
| Method | Endpoint | 설명 |
|--------|----------|------|
| STOMP | `/app/chat/{groupId}/send` | 메시지 발송 |
| GET | `/api/chat/{groupId}/history` | 메시지 히스토리 (페이징) |
| GET | `/api/chat/{groupId}/recent` | 최근 메시지 50개 |

### 알림 (`/api/notifications`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/notifications` | 알림 목록 |
| GET | `/api/notifications/unread-count` | 미읽음 개수 |
| PATCH | `/api/notifications/{id}/read` | 읽음 처리 |
| PATCH | `/api/notifications/read-all` | 전체 읽음 처리 |

### 결제 (`/api/payments`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/payments/confirm` | 결제 승인 (Toss) |
| GET | `/api/payments/{paymentKey}` | 결제 상세 |
| GET | `/api/payments/my` | 내 결제 내역 |
| POST | `/api/payments/{paymentKey}/cancel` | 결제 취소 |

---

## 📁 프로젝트 구조

```
backend/src/main/java/com/nearsplit/
├── common/                    # 공통 모듈 (JWT, 예외처리, 응답 래퍼)
├── config/                    # 설정 (Security, WebSocket, QueryDSL)
├── domain/
│   ├── user/                  # 사용자 (인증 포함)
│   ├── split_group/           # 소분 그룹 + 참여자
│   ├── product/               # 상품
│   ├── chat/                  # 채팅
│   ├── notification/          # 알림
│   └── payment/               # 결제
└── external/
    ├── juso/                  # 도로명주소 API
    ├── vworld/                # VWorld 지오코딩 API
    └── toss/                  # 토스페이먼츠 API

frontend/src/
├── views/                     # 14개 페이지
├── api/                       # 9개 API 모듈
├── components/                # NavBar 등
└── router/                    # Vue Router (인증 가드)
```

---

## 🚀 빠른 시작

### 요구사항
- Java 17 이상
- Gradle 8.x
- Node.js 18 이상

### 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

### 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

### H2 Console (개발 환경)
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (비어있음)
```

---

## 프로젝트 정보

**프로젝트 상태**: 🟢 개발 완료 (핵심 기능)
**마지막 업데이트**: 2026-02-28
**구현 완료**: 인증 / 소분 그룹 / 상품 / 채팅 / 알림 / 결제 / 외부 API 연동
