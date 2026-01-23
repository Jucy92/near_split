<template>
  <div class="container py-4">
    <!-- 상단 헤더 -->
    <div class="d-flex align-items-center mb-4">
      <router-link to="/groups" class="btn btn-outline-secondary me-3">&larr; 목록</router-link>
      <h3 class="mb-0">소분 그룹 생성</h3>
    </div>

    <!-- 생성 폼 -->
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-6">
        <div class="card shadow">
          <div class="card-body p-4">
            <!-- 에러 메시지 -->
            <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

            <form @submit.prevent="handleCreate">
              <!-- 제목 -->
              <div class="mb-3">
                <label for="title" class="form-label">그룹 제목 *</label>
                <input
                  type="text"
                  class="form-control"
                  id="title"
                  v-model="form.title"
                  placeholder="예: 코스트코 영양제 20인분 모집"
                  required
                />
              </div>

              <!-- 총 금액 -->
              <div class="mb-3">
                <label for="totalPrice" class="form-label">총 금액 (원) *</label>
                <input
                  type="number"
                  class="form-control"
                  id="totalPrice"
                  v-model.number="form.totalPrice"
                  placeholder="150000"
                  min="0"
                  required
                />
              </div>

              <!-- 최대 참여자 수 -->
              <div class="mb-3">
                <label for="maxParticipants" class="form-label">최대 참여자 수 *</label>
                <input
                  type="number"
                  class="form-control"
                  id="maxParticipants"
                  v-model.number="form.maxParticipants"
                  placeholder="20"
                  min="2"
                  max="100"
                  required
                />
                <div class="form-text">1인당 예상 금액: {{ pricePerPerson }}원</div>
              </div>

              <!-- 픽업 장소 -->
              <div class="mb-3">
                <label for="pickupLocation" class="form-label">픽업 장소</label>
                <input
                  type="text"
                  class="form-control"
                  id="pickupLocation"
                  v-model="form.pickupLocation"
                  placeholder="강남역 2번 출구"
                />
              </div>

              <!-- 모집 마감일 -->
              <div class="mb-4">
                <label for="closedAt" class="form-label">모집 마감일</label>
                <input
                  type="date"
                  class="form-control"
                  id="closedAt"
                  v-model="form.closedAt"
                  :min="minDate"
                />
              </div>

              <!-- 버튼 -->
              <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary flex-grow-1" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  {{ loading ? '생성 중...' : '그룹 생성' }}
                </button>
                <router-link to="/groups" class="btn btn-outline-secondary">취소</router-link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { createGroup } from '../api/group'

export default {
  name: 'GroupCreateView',

  data() {
    return {
      form: {
        title: '',
        totalPrice: null,
        maxParticipants: null,
        pickupLocation: '',
        closedAt: ''
      },
      loading: false,
      errorMessage: ''
    }
  },

  computed: {
    pricePerPerson() {
      if (this.form.totalPrice && this.form.maxParticipants) {
        return Math.ceil(this.form.totalPrice / this.form.maxParticipants).toLocaleString()
      }
      return '0'
    },
    minDate() {
      // 오늘 날짜를 YYYY-MM-DD 형식으로
      const today = new Date()
      return today.toISOString().split('T')[0]
    }
  },

  methods: {
    async handleCreate() {
      this.loading = true
      this.errorMessage = ''

      try {
        const response = await createGroup(this.form)
        const createdGroup = response.data
        console.log(response);
        console.log(createdGroup);
        // 생성된 그룹 상세 페이지로 이동
        this.$router.push(`/groups/${createdGroup.id}`)
      } catch (error) {
        console.error('그룹 생성 실패:', error)
        if (error.response?.data?.message) {
          this.errorMessage = error.response.data.message
        } else {
          this.errorMessage = '그룹 생성에 실패했습니다.'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
