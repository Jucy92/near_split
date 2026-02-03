<!--
  파일: RegisterView.vue
  설명: 회원가입 페이지
        - 이름, 이메일, 비밀번호 입력 폼
        - 비밀번호 확인 (프론트에서만 검증, 백엔드로 전송 X)
        - 회원가입 API 호출 후 로그인 페이지로 이동

  ==================== 페이지 접근 흐름 ====================
  1. LoginView에서 "회원가입" 링크 클릭
  2. router가 /register로 이동
  3. RegisterView 렌더링
  4. 사용자가 정보 입력 후 회원가입 버튼 클릭
  5. handleRegister() 실행
     - 비밀번호 일치 확인 (프론트 검증)
     - POST /api/auth/register 호출
  6. 회원가입 성공 시 2초 후 /login으로 리다이렉트

  ==================== API 목록 ====================
  | 기능     | 메서드 | 엔드포인트        | 호출 함수   |
  |----------|--------|-------------------|-------------|
  | 회원가입 | POST   | /api/auth/register| register()  |

  ==================== 백엔드 요청/응답 구조 ====================
  POST /api/auth/register
  Request Body:
  {
    "name": "홍길동",
    "email": "user@example.com",
    "password": "1234"
  }
  Response: 200 OK (body 없음)

  ==================== 프론트 전용 필드 ====================
  - passwordConfirm: 비밀번호 확인용 (백엔드로 전송 X)
    → 비밀번호 일치 확인은 프론트에서만 수행
    → 백엔드 RegisterRequest에는 name, email, password만 포함
-->
<template>
  <div class="container">
    <div class="row justify-content-center align-items-center min-vh-100">
      <!-- 로그인 페이지와 동일한 반응형 너비 -->
      <div class="col-12 col-sm-10 col-md-8 col-lg-5">
        <div class="card shadow">
          <div class="card-body p-4">
            <h2 class="text-center mb-4">NearSplit</h2>
            <h5 class="text-center text-muted mb-4">회원가입</h5>

            <!-- 에러 메시지 -->
            <div v-if="errorMessage" class="alert alert-danger" role="alert">
              {{ errorMessage }}
            </div>

            <!-- 성공 메시지 -->
            <div v-if="successMessage" class="alert alert-success" role="alert">
              {{ successMessage }}
            </div>

            <!-- 회원가입 폼 -->
            <form @submit.prevent="handleRegister">
              <!-- 이름 -->
              <div class="mb-3">
                <label for="name" class="form-label">이름</label>
                <input
                  type="text"
                  class="form-control"
                  id="name"
                  v-model="registerForm.name"
                  placeholder="홍길동"
                  required
                />
              </div>

              <!-- 이메일 -->
              <div class="mb-3">
                <label for="email" class="form-label">이메일</label>
                <input
                  type="email"
                  class="form-control"
                  id="email"
                  v-model="registerForm.email"
                  placeholder="example@email.com"
                  required
                />
              </div>

              <!-- 비밀번호 -->
              <div class="mb-3">
                <label for="password" class="form-label">비밀번호</label>
                <input
                  type="password"
                  class="form-control"
                  id="password"
                  v-model="registerForm.password"
                  placeholder="비밀번호를 입력하세요"
                  required
                />
              </div>

              <!-- 비밀번호 확인 -->
              <div class="mb-3">
                <label for="passwordConfirm" class="form-label">비밀번호 확인</label>
                <input
                  type="password"
                  class="form-control"
                  id="passwordConfirm"
                  v-model="registerForm.passwordConfirm"
                  placeholder="비밀번호를 다시 입력하세요"
                  required
                />
              </div>

              <!-- 회원가입 버튼 -->
              <button
                type="submit"
                class="btn btn-primary w-100 mb-3"
                :disabled="loading"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                {{ loading ? '가입 중...' : '회원가입' }}
              </button>

              <!-- 로그인 링크 -->
              <div class="text-center">
                <span class="text-muted">이미 계정이 있으신가요?</span>
                <router-link to="/login" class="ms-2">로그인</router-link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { register } from '../api/auth'

export default {
  name: 'RegisterView',
  data() {
    return {
      registerForm: {
        name: '',
        email: '',
        password: '',
        passwordConfirm: ''
      },
      loading: false,
      errorMessage: '',
      successMessage: ''
    }
  },
  methods: {
    async handleRegister() {
      this.loading = true
      this.errorMessage = ''
      this.successMessage = ''

      // 비밀번호 확인
      if (this.registerForm.password !== this.registerForm.passwordConfirm) {
        this.errorMessage = '비밀번호가 일치하지 않습니다.'
        this.loading = false
        return
      }

      try {
        // passwordConfirm은 백엔드로 보내지 않음
        const { passwordConfirm, ...data } = this.registerForm
        await register(data)

        // 회원가입 성공
        this.successMessage = '회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.'

        setTimeout(() => {
          this.$router.push('/login')
        }, 2000)
      } catch (error) {
        // 회원가입 실패
        console.error('회원가입 실패:', error)

        if (error.response && error.response.data) {
          this.errorMessage = error.response.data.message || '회원가입에 실패했습니다.'
        } else {
          this.errorMessage = '네트워크 오류가 발생했습니다.'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.min-vh-100 {
  min-height: 100vh;
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
