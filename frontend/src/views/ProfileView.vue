<template>
  <div class="container py-4">
    <!-- 뒤로가기 + 제목 -->
    <div class="d-flex align-items-center mb-4">
      <router-link to="/home" class="btn btn-outline-secondary me-3">
        &larr; 홈
      </router-link>
      <h3 class="mb-0">프로필</h3>
    </div>

    <!-- 로딩 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩중...</span>
      </div>
    </div>

    <!-- 프로필 카드 -->
    <div v-else class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-6">
        <div class="card shadow">
          <div class="card-body p-4">
            <!-- 에러 메시지 -->
            <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
            <!-- 성공 메시지 -->
            <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>

            <!-- 수정 모드가 아닐 때 -->
            <div v-if="!editMode">
              <div class="text-center mb-4">
                <div class="bg-primary text-white rounded-circle d-inline-flex align-items-center justify-content-center"
                     style="width: 80px; height: 80px; font-size: 2rem;">
                  {{ user?.name?.charAt(0) || '?' }}
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label text-muted small">닉네임</label>
                <p class="mb-0 fs-5">{{ user?.nickname || '-' }}</p>
              </div>

              <div class="mb-3">
                <label class="form-label text-muted small">이름</label>
                <p class="mb-0 fs-5">{{ user?.name || '-' }}</p>
              </div>

              <div class="mb-4">
                <label class="form-label text-muted small">이메일</label>
                <p class="mb-0 fs-5">{{ user?.email || '-' }}</p>
              </div>

              <button class="btn btn-primary w-100" @click="startEdit">
                프로필 수정
              </button>
            </div>

            <!-- 수정 모드일 때 -->
            <form v-else @submit.prevent="handleUpdate">
              <div class="mb-3">
                <label for="name" class="form-label">이름</label>
                <input
                  type="text"
                  class="form-control"
                  id="name"
                  v-model="editForm.name"
                  required
                />
              </div>

              <div class="mb-4">
                <label for="email" class="form-label">이메일</label>
                <input
                  type="email"
                  class="form-control"
                  id="email"
                  v-model="editForm.email"
                  required
                />
              </div>

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
      user: null,
      loading: true,
      editMode: false,
      updating: false,
      editForm: {
        name: '',
        email: ''
      },
      errorMessage: '',
      successMessage: ''
    }
  },

  async mounted() {
    await this.loadProfile()
  },

  methods: {
    async loadProfile() {
      this.loading = true
      try {
        const response = await getMyProfile()
        this.user = response.data
      } catch (error) {
        console.error('프로필 로드 실패:', error)
        this.errorMessage = '프로필을 불러오는데 실패했습니다.'
      } finally {
        this.loading = false
      }
    },

    startEdit() {
      this.editForm.name = this.user.name
      this.editForm.email = this.user.email
      this.editMode = true
      this.errorMessage = ''
      this.successMessage = ''
    },

    cancelEdit() {
      this.editMode = false
      this.errorMessage = ''
    },

    async handleUpdate() {
      this.updating = true
      this.errorMessage = ''
      this.successMessage = ''

      try {
        const response = await updateMyProfile(this.editForm)
        this.user = response.data
        this.editMode = false
        this.successMessage = '프로필이 수정되었습니다.'
      } catch (error) {
        console.error('프로필 수정 실패:', error)
        if (error.response?.data?.message) {
          this.errorMessage = error.response.data.message
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
