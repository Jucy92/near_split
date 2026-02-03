<!--
  파일: LoginView.vue
  설명: 로그인 페이지
        - 이메일/비밀번호 입력 폼
        - 로그인 API 호출 및 JWT 토큰 쿠키 저장
        - 회원가입 페이지 링크

  ==================== 페이지 접근 흐름 ====================
  1. 사용자가 브라우저에서 / 또는 /login 접속
  2. router/index.js의 beforeEach 가드 실행
     - 로그인 상태 체크 (localStorage.getItem('isLoggedIn'))
     - 미로그인 + 인증 필요 페이지 → /login으로 리다이렉트
  3. LoginView 렌더링
  4. 사용자가 이메일/비밀번호 입력 후 로그인 버튼 클릭
  5. handleLogin() 실행 → POST /api/auth/login
  6. 백엔드에서 JWT 토큰을 쿠키로 설정 (Set-Cookie 헤더)
     - accessToken: 30분 유효
     - refreshToken: 7일 유효
  7. 프론트에서 localStorage에 'isLoggedIn' 저장
  8. /home으로 리다이렉트

  ==================== API 목록 ====================
  | 기능     | 메서드 | 엔드포인트      | 호출 함수  |
  |----------|--------|-----------------|------------|
  | 로그인   | POST   | /api/auth/login | login()    |

  ==================== 백엔드 응답 구조 ====================
  POST /api/auth/login 응답:
  {
    "success": true,
    "data": {
      "userResponse": {
        "id": 1,
        "email": "user@example.com",
        "name": "홍길동",
        "nickname": "홍길동",
        "address": "서울시 강남구",
        "phone": "010-1234-5678"
      }
    }
  }
  + Set-Cookie: accessToken=xxx; HttpOnly; Secure; SameSite=None
  + Set-Cookie: refreshToken=xxx; HttpOnly; Secure; SameSite=None

  ==================== Vue 파일 구조 ====================
  1. <template>: HTML (화면 구조)
  2. <script>: JavaScript (로직, 상태, 메서드)
  3. <style>: CSS (스타일, scoped면 이 컴포넌트만 적용)
-->
<template>
  <!-- container: Bootstrap 컨테이너 (가운데 정렬, 반응형) -->
  <div class="container">
    <!-- row: Bootstrap 행 -->
    <!-- justify-content-center: 가로 가운데 정렬 -->
    <!-- align-items-center: 세로 가운데 정렬 -->
    <!-- min-vh-100: 최소 높이 100vh (화면 전체 높이) -->
    <div class="row justify-content-center align-items-center min-vh-100">
      <!-- col-12: 작은 화면에서 전체 너비 -->
      <!-- col-sm-10: 작은~중간 화면에서 10칸 (조금 여백) -->
      <!-- col-md-8: 중간 화면에서 8칸 -->
      <!-- col-lg-5: 큰 화면에서 5칸 (적당한 너비) -->
      <div class="col-12 col-sm-10 col-md-8 col-lg-5">
        <!-- card: Bootstrap 카드 컴포넌트 -->
        <!-- shadow: 그림자 효과 -->
        <div class="card shadow">
          <div class="card-body p-4">
            <h2 class="text-center mb-4">NearSplit</h2>
            <h5 class="text-center text-muted mb-4">로그인</h5>

            <!-- v-if: 조건부 렌더링 (errorMessage가 있을 때만 표시) -->
            <div v-if="errorMessage" class="alert alert-danger" role="alert">
              <!-- {{ }}: 데이터 출력 (Mustache 문법) -->
              {{ errorMessage }}
            </div>

            <!-- @submit.prevent: form 제출 이벤트 리스닝 + 기본 동작 방지 -->
            <!-- 기본 동작: 페이지 새로고침 → prevent로 막음 -->
            <form @submit.prevent="handleLogin">
              <!-- 이메일 입력 -->
              <div class="mb-3">
                <label for="email" class="form-label">이메일</label>
                <!-- v-model: 양방향 데이터 바인딩 -->
                <!-- 입력값 ↔ loginForm.email 자동 동기화 -->
                <input
                  type="email"
                  class="form-control"
                  id="email"
                  v-model="loginForm.email"
                  placeholder="example@email.com"
                  required
                />
              </div>

              <!-- 비밀번호 입력 -->
              <div class="mb-3">
                <label for="password" class="form-label">비밀번호</label>
                <input
                  type="password"
                  class="form-control"
                  id="password"
                  v-model="loginForm.password"
                  placeholder="비밀번호를 입력하세요"
                  required
                />
              </div>

              <!-- 로그인 버튼 -->
              <button
                type="submit"
                class="btn btn-primary w-100 mb-3"
                :disabled="loading"
              >
                <!-- v-if: loading이 true일 때만 표시 -->
                <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                <!-- 삼항 연산자: loading이 true면 "로그인 중...", false면 "로그인" -->
                {{ loading ? '로그인 중...' : '로그인' }}
              </button>

              <!-- 회원가입 링크 -->
              <div class="text-center">
                <span class="text-muted">계정이 없으신가요?</span>
                <!-- router-link: Vue Router 링크 (페이지 이동, 새로고침 없음) -->
                <router-link to="/register" class="ms-2">회원가입</router-link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// API 함수 import
import { login } from '../api/auth'

// ===========================
// Vue 컴포넌트 정의 (Options API)
// ===========================
export default {
  name: 'LoginView',  // 컴포넌트 이름 (디버깅 시 유용)

  // ===========================
  // data(): 컴포넌트의 상태(데이터) 정의
  // ===========================
  // 반환된 객체의 속성들이 반응형 데이터가 됨
  // 값이 변경되면 자동으로 화면 업데이트
  data() {
    return {
      // 로그인 폼 데이터 (v-model로 연결)
      loginForm: {
        email: '',     // 이메일 입력값
        password: ''   // 비밀번호 입력값
      },
      loading: false,      // 로그인 중 여부 (버튼 비활성화용)
      errorMessage: ''     // 에러 메시지 (로그인 실패 시 표시)
    }
  },

  // ===========================
  // methods: 컴포넌트의 메서드 정의
  // ===========================
  methods: {
    // 로그인 처리 함수 (form 제출 시 실행)
    async handleLogin() {
      this.loading = true       // 로딩 시작 (버튼 비활성화)
      this.errorMessage = ''    // 이전 에러 메시지 초기화

      try {
        // login API 호출 (axios 사용)
        // POST /api/auth/login
        // body: { email: '...', password: '...' }
        const response = await login(this.loginForm)

        // 로그인 성공
        console.log('로그인 성공:', response.data)
        // response.data 구조:
        // {
        //   success: true,
        //   data: { userResponse: { id: 1, name: '홍길동', email: '...' } }
        // }

        // 쿠키에 JWT 토큰 자동 저장됨 (백엔드에서 Set-Cookie 헤더)
        localStorage.setItem('isLoggedIn', 'true')  // 추가

        // 그룹 목록 페이지로 이동 (Vue Router 사용)
        // this.$router: Vue Router 인스턴스
        // push(): 페이지 이동 (브라우저 히스토리에 추가)
        this.$router.push('/home')

      } catch (error) {
        // 로그인 실패
        console.error('로그인 실패:', error)

        // 에러 응답에서 메시지 추출
        if (error.response && error.response.data) {
          // 백엔드에서 보낸 에러 메시지
          this.errorMessage = error.response.data.message || '로그인에 실패했습니다.'
        } else {
          // 네트워크 에러
          this.errorMessage = '네트워크 오류가 발생했습니다.'
        }

      } finally {
        // 성공/실패 상관없이 실행
        this.loading = false  // 로딩 종료
      }
    }
  }
}
</script>

<style scoped>
/* scoped: 이 컴포넌트에만 스타일 적용 (다른 컴포넌트에 영향 없음) */

.min-vh-100 {
  min-height: 100vh;  /* 최소 높이: 화면 전체 높이 */
}

/* 입력 필드 기본 스타일 (반응형은 style.css에서 처리) */
.form-control {
  padding: 0.75rem 1rem;
  font-size: 1rem;
  line-height: 1.5;
}

/* 버튼 기본 스타일 */
.btn {
  padding: 0.75rem 1rem;
  font-size: 1rem;
}
</style>
