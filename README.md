# 🛒 NearSplit

**대용량 상품 공동구매 지역 기반 소분 플랫폼**

쿠팡, 코스트코 등에서 판매하는 대용량 상품을 근처 이웃과 함께 나눠 구매하는 서비스입니다.

---

## 📌 프로젝트 개요

- **서비스명**: NearSplit
- **핵심 가치**: 대용량 상품을 반경 3km 내 근처 사용자와 소분하여 경제적이고 친환경적인 구매 실현
- **주요 기능**:
  - 🏘️ 지역 기반 매칭 (반경 3km)
  - 🛡️ 에스크로 결제 시스템
  - 🔗 외부 상품 API 연동 (쿠팡/코스트코)
  - 📱 웹 → 모바일 앱 확장 계획

---

## 🗂️ 프로젝트 문서 구조

프로젝트는 체계적인 단계별 문서로 관리됩니다:

### 📋 기획 및 설계 문서 (`claudedocs/`)
1. **[PROJECT_ROADMAP.md](./claudedocs/PROJECT_ROADMAP.md)** - 전체 프로젝트 로드맵 및 진행 현황
2. **[01_SERVICE_PLANNING.md](./claudedocs/01_SERVICE_PLANNING.md)** - 서비스 기획 단계
3. **[02_REQUIREMENTS_USECASES.md](./claudedocs/02_REQUIREMENTS_USECASES.md)** - 요구사항 및 유스케이스
4. **03_DESIGN_PHASE.md** - 설계 단계 (화면·ERD·API·아키텍처) [예정]
5. **04_DEVELOPMENT.md** - 개발 단계 [예정]
6. **05_BUILD_DEPLOY.md** - 빌드/배포 단계 [예정]
7. **06_OPERATIONS.md** - 운영 단계 [예정]
8. **07_MAINTENANCE.md** - 유지보수 및 확장 단계 [예정]

---

## 🎯 현재 진행 단계

**✅ 1단계: 서비스 기획** (진행 중)
- [x] 프로젝트 문서 구조 수립
- [ ] 비즈니스 모델 확정
- [ ] MVP 기능 범위 확정

**⬜ 2단계: 요구사항 정리** (대기)
**⬜ 3단계: 설계** (대기)
**⬜ 4단계: 개발** (대기)

---

## 🚀 빠른 시작 (추후 업데이트)

```bash
# 저장소 클론
git clone [repository-url]

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

---

## 🛠️ 기술 스택 (예정)

### Frontend
- React / Next.js
- TypeScript
- Tailwind CSS

### Backend
- Node.js / Express
- PostgreSQL (PostGIS for geospatial)
- Redis (caching)

### Infrastructure
- AWS / Vercel
- Docker
- GitHub Actions (CI/CD)

---

## 📞 문의 및 협업

프로젝트 진행 상황은 `claudedocs/PROJECT_ROADMAP.md`에서 확인할 수 있습니다.

---

**마지막 업데이트**: 2025-11-26
**프로젝트 상태**: 🟡 기획 단계
