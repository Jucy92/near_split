<!--
  파일: ProfileView.vue
  설명: 사용자 프로필 조회/수정 페이지
        - 현재 로그인한 사용자 정보 표시
        - 프로필 수정 기능 (닉네임, 전화번호, 주소, 지역, 비밀번호)
        - 이름과 이메일은 수정 불가 (읽기 전용)

  ==================== 페이지 접근 흐름 ====================
  1. HomeView 또는 NavBar에서 "프로필" 클릭
  2. router가 /profile로 이동
  3. ProfileView 렌더링
  4. mounted()에서 loadProfile() 호출 → GET /api/users/me
  5. 프로필 정보 표시
  6. "프로필 수정" 버튼 클릭 → editMode = true → 수정 폼 표시
  7. 수정 후 "저장" → handleUpdate() → PATCH /api/users/me

  ==================== 데이터 흐름 ====================
  mounted() → loadProfile() → GET /api/users/me → this.user
  handleUpdate() → PATCH /api/users/me → this.user 갱신

  ==================== API 목록 ====================
  | 기능         | 메서드 | 엔드포인트    | 호출 함수        |
  |--------------|--------|---------------|------------------|
  | 프로필 조회  | GET    | /api/users/me | getMyProfile()   |
  | 프로필 수정  | PATCH  | /api/users/me | updateMyProfile()|

  ==================== 백엔드 응답 구조 ====================
  GET /api/users/me 응답 (UserResponse):
  {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "nickname": "동동이",
    "phone": "010-1234-5678",
    "address": "서울시 강남구",
    "location": "강남구",
    "profileImage": "https://example.com/image.jpg"
  }

  ==================== 수정 가능/불가 필드 ====================
  | 필드         | 수정 가능 | 비고                    |
  |--------------|-----------|-------------------------|
  | name         | ❌        | 회원가입 시 설정, 변경 불가 |
  | email        | ❌        | 회원가입 시 설정, 변경 불가 |
  | nickname     | ✅        | 필수                    |
  | phone        | ✅        | 선택                    |
  | address      | ✅        | 선택                    |
  | location     | ✅        | 선택                    |
  | profileImage | ✅        | URL 형식               |
  | password     | ✅        | 변경 시에만 입력        |
-->
<template>
  <div class="container py-4">
    <!-- 로딩 스피너 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩중...</span>
      </div>
    </div>

    <!-- 프로필 카드 -->
    <div v-else class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-6">
        <div class="card shadow">
          <div class="card-header bg-white">
            <h4 class="mb-0">내 프로필</h4>
          </div>
          <div class="card-body p-4">
            <!-- 에러 메시지 -->
            <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
            <!-- 성공 메시지 -->
            <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>

            <!-- 수정 모드가 아닐 때: 프로필 보기 -->
            <div v-if="!editMode">
              <!-- 프로필 이미지 영역 -->
              <div class="text-center mb-4">
                <div
                  v-if="user?.profileImage"
                  class="rounded-circle d-inline-block overflow-hidden"
                  style="width: 100px; height: 100px;"
                >
                  <img :src="user.profileImage" alt="프로필" class="w-100 h-100 object-fit-cover" />
                </div>
                <div
                  v-else
                  class="bg-primary text-white rounded-circle d-inline-flex align-items-center justify-content-center"
                  style="width: 100px; height: 100px; font-size: 2.5rem;"
                >
                  {{ user?.nickname?.charAt(0) || '?' }}
                </div>
              </div>

              <!-- 기본 정보 (수정 불가) -->
              <div class="mb-3 p-3 bg-light rounded">
                <div class="row">
                  <div class="col-6">
                    <label class="form-label text-muted small mb-0">이름</label>
                    <p class="mb-0 fw-bold">{{ user?.name || '-' }}</p>
                  </div>
                  <div class="col-6">
                    <label class="form-label text-muted small mb-0">이메일</label>
                    <p class="mb-0 fw-bold">{{ user?.email || '-' }}</p>
                  </div>
                </div>
                <small class="text-muted d-block mt-2">* 이름과 이메일은 변경할 수 없습니다.</small>
              </div>

              <!-- 수정 가능한 정보 -->
              <div class="mb-3">
                <label class="form-label text-muted small">닉네임</label>
                <p class="mb-0 fs-5">{{ user?.nickname || '-' }}</p>
              </div>

              <div class="mb-3">
                <label class="form-label text-muted small">전화번호</label>
                <p class="mb-0 fs-5">{{ user?.phone || '-' }}</p>
              </div>

              <div class="mb-3">
                <label class="form-label text-muted small">주소</label>
                <p class="mb-0 fs-5">{{ user?.address || '-' }}</p>
              </div>

              <div class="mb-4">
                <label class="form-label text-muted small">지역</label>
                <p class="mb-0 fs-5">{{ user?.location || '-' }}</p>
              </div>

              <button class="btn btn-primary w-100" @click="startEdit">
                프로필 수정
              </button>
            </div>

            <!-- 수정 모드일 때: 폼 표시 -->
            <form v-else @submit.prevent="handleUpdate">
              <!-- 프로필 이미지 URL 입력 -->
              <div class="mb-3">
                <label for="profileImage" class="form-label">프로필 이미지 URL</label>
                <input
                  type="url"
                  class="form-control"
                  id="profileImage"
                  v-model="editForm.profileImage"
                  placeholder="https://example.com/image.jpg"
                />
                <small class="text-muted">이미지 URL을 입력하세요 (선택사항)</small>
              </div>

              <!-- 닉네임 -->
              <div class="mb-3">
                <label for="nickname" class="form-label">닉네임 <span class="text-danger">*</span></label>
                <input
                  type="text"
                  class="form-control"
                  id="nickname"
                  v-model="editForm.nickname"
                  required
                  maxlength="20"
                />
              </div>

              <!-- 전화번호 -->
              <div class="mb-3">
                <label for="phone" class="form-label">전화번호</label>
                <input
                  type="tel"
                  class="form-control"
                  id="phone"
                  v-model="editForm.phone"
                  placeholder="010-1234-5678"
                />
              </div>

              <!-- 주소 -->
              <div class="mb-3">
                <label for="address" class="form-label">주소</label>
                <input
                  type="text"
                  class="form-control"
                  id="address"
                  v-model="editForm.address"
                  placeholder="서울시 강남구..."
                />
              </div>

              <!-- 지역 -->
              <div class="mb-3">
                <label for="location" class="form-label">지역</label>
                <input
                  type="text"
                  class="form-control"
                  id="location"
                  v-model="editForm.location"
                  placeholder="강남구"
                />
              </div>

              <!-- 비밀번호 변경 (선택사항) -->
              <div class="mb-4">
                <label for="password" class="form-label">새 비밀번호</label>
                <input
                  type="password"
                  class="form-control"
                  id="password"
                  v-model="editForm.password"
                  placeholder="변경 시에만 입력"
                  minlength="4"
                />
                <small class="text-muted">비밀번호를 변경하려면 입력하세요 (선택사항)</small>
              </div>

              <!-- 버튼 영역 -->
              <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary flex-grow-1" :disabled="updating">
                  <span v-if="updating" class="spinner-border spinner-border-sm me-2"></span>
                  {{ updating ? '저장 중...' : '저장' }}
                </button>
                <button type="button" class="btn btn-outline-secondary" @click="cancelEdit">
                  취소
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getMyProfile, updateMyProfile } from '../api/user'

export default {
  name: 'ProfileView',

  data() {
    return {
      user: null,           // 현재 사용자 정보
      loading: true,        // 로딩 상태
      editMode: false,      // 수정 모드 여부
      updating: false,      // 저장 중 상태
      editForm: {
        nickname: '',       // 닉네임
        phone: '',          // 전화번호
        address: '',        // 주소
        location: '',       // 지역
        profileImage: '',   // 프로필 이미지 URL
        password: ''        // 새 비밀번호 (선택)
      },
      errorMessage: '',
      successMessage: ''
    }
  },

  async mounted() {
    await this.loadProfile()
  },

  methods: {
    // 프로필 로드
    async loadProfile() {
      this.loading = true
      try {
        const response = await getMyProfile()
        // UserController는 ApiResponse 없이 직접 반환
        this.user = response.data
      } catch (error) {
        console.error('프로필 로드 실패:', error)
        this.errorMessage = '프로필을 불러오는데 실패했습니다.'
      } finally {
        this.loading = false
      }
    },

    // 수정 모드 시작
    startEdit() {
      // 현재 사용자 정보로 폼 초기화
      this.editForm.nickname = this.user.nickname || ''
      this.editForm.phone = this.user.phone || ''
      this.editForm.address = this.user.address || ''
      this.editForm.location = this.user.location || ''
      this.editForm.profileImage = this.user.profileImage || ''
      this.editForm.password = ''  // 비밀번호는 항상 빈 값
      this.editMode = true
      this.errorMessage = ''
      this.successMessage = ''
    },

    // 수정 취소
    cancelEdit() {
      this.editMode = false
      this.errorMessage = ''
    },

    // 프로필 수정 저장
    async handleUpdate() {
      this.updating = true
      this.errorMessage = ''
      this.successMessage = ''

      try {
        // 비밀번호가 비어있으면 요청에서 제외
        const updateData = {
          nickname: this.editForm.nickname,
          phone: this.editForm.phone || null,
          address: this.editForm.address || null,
          location: this.editForm.location || null,
          profileImage: this.editForm.profileImage || null
        }

        // 비밀번호가 입력된 경우에만 포함
        if (this.editForm.password) {
          updateData.password = this.editForm.password
        }

        const response = await updateMyProfile(updateData)
        this.user = response.data
        this.editMode = false
        this.successMessage = '프로필이 수정되었습니다.'
      } catch (error) {
        console.error('프로필 수정 실패:', error)
        if (error.response?.data?.message) {
          this.errorMessage = error.response.data.message
        } else if (error.response?.data?.errors) {
          // validation 에러
          this.errorMessage = Object.values(error.response.data.errors).join(', ')
        } else {
          this.errorMessage = '프로필 수정에 실패했습니다.'
        }
      } finally {
        this.updating = false
      }
    }
  }
}
</script>
