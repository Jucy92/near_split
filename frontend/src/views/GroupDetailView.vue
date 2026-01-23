<template>
  <div class="container py-4">
    <!-- 상단 헤더 -->
    <div class="d-flex align-items-center mb-4">
      <router-link to="/groups" class="btn btn-outline-secondary me-3">&larr; 목록</router-link>
      <h3 class="mb-0">그룹 상세</h3>
    </div>

    <!-- 로딩 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩중...</span>
      </div>
    </div>

    <!-- 그룹 정보 -->
    <div v-else-if="group" class="row">
      <!-- 왼쪽: 그룹 정보 카드 -->
      <div class="col-12 col-lg-8">
        <div class="card shadow-sm mb-4">
          <div class="card-body">
            <!-- 에러/성공 메시지 -->
            <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
            <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>

            <!-- 제목 + 상태 -->
            <div class="d-flex justify-content-between align-items-start mb-3">
              <h4 class="card-title mb-0">{{ group.title }}</h4>
              <span class="badge fs-6" :class="getStatusBadgeClass(group.status)">
                {{ getStatusText(group.status) }}
              </span>
            </div>

            <!-- 정보 그리드 -->
            <div class="row g-3 mb-4">
              <div class="col-6">
                <div class="text-muted small">총 금액</div>
                <div class="fs-5 fw-bold text-primary">{{ formatPrice(group.totalPrice) }}원</div>
              </div>
              <div class="col-6">
                <div class="text-muted small">1인당 금액</div>
                <div class="fs-5 fw-bold">{{ pricePerPerson }}원</div>
              </div>
              <div class="col-6">
                <div class="text-muted small">참여 현황</div>
                <div class="fs-5">
                  <span class="fw-bold">{{ group.currentParticipants }}</span>
                  <span class="text-muted"> / {{ group.maxParticipants }}명</span>
                </div>
              </div>
              <div class="col-6">
                <div class="text-muted small">픽업 장소</div>
                <div class="fs-5">{{ group.pickupLocation || '미정' }}</div>
              </div>
            </div>

            <!-- 진행바 -->
            <div class="progress mb-4" style="height: 20px;">
              <div
                class="progress-bar"
                :class="progressBarClass"
                :style="{ width: progressPercent + '%' }"
              >
                {{ progressPercent }}%
              </div>
            </div>

            <!-- 액션 버튼 -->
            <div class="d-flex gap-2 flex-wrap">
              <!-- 호스트가 아닐 때: 참여/취소 버튼 -->
              <template v-if="!isHost">
                <button
                  v-if="!isParticipant && group.status === 'RECRUITING'"
                  class="btn btn-primary"
                  @click="handleJoin"
                  :disabled="actionLoading"
                >
                  참여 신청
                </button>
                <button
                  v-if="isParticipant && myParticipantStatus === 'PENDING'"
                  class="btn btn-outline-danger"
                  @click="handleCancelJoin"
                  :disabled="actionLoading"
                >
                  참여 취소
                </button>
                <span v-if="myParticipantStatus === 'APPROVED'" class="badge bg-success fs-6 align-self-center">
                  참여 승인됨
                </span>
                <span v-if="myParticipantStatus === 'REJECTED'" class="badge bg-danger fs-6 align-self-center">
                  참여 거절됨
                </span>
              </template>

              <!-- 호스트일 때: 수정/삭제 버튼 -->
              <template v-if="isHost">
                <button class="btn btn-outline-primary" @click="showEditModal = true">수정</button>
                <button class="btn btn-outline-danger" @click="handleDelete" :disabled="actionLoading">삭제</button>
              </template>

              <!-- 채팅 버튼 (참여자만) -->
              <router-link
                v-if="isParticipant || isHost"
                :to="`/chat/${group.id}`"
                class="btn btn-outline-secondary"
              >
                채팅방
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- 오른쪽: 참여자 목록 (호스트만) -->
      <div class="col-12 col-lg-4" v-if="isHost">
        <div class="card shadow-sm">
          <div class="card-header bg-white">
            <h5 class="mb-0">참여자 관리</h5>
          </div>
          <div class="card-body">
            <div v-if="participants.length === 0" class="text-muted text-center py-3">
              아직 참여자가 없습니다.
            </div>
            <ul v-else class="list-group list-group-flush">
              <li
                v-for="participant in participants"
                :key="participant.id"
                class="list-group-item d-flex justify-content-between align-items-center"
              >
                <div>
                  <div class="fw-bold">{{ participant.userName || '참여자' }}</div>
                  <small class="text-muted">{{ participant.userEmail }}</small>
                </div>
                <div>
                  <!-- 상태에 따른 버튼 -->
                  <span v-if="participant.status === 'APPROVED'" class="badge bg-success">승인됨</span>
                  <span v-else-if="participant.status === 'REJECTED'" class="badge bg-danger">거절됨</span>
                  <div v-else class="btn-group btn-group-sm">
                    <button
                      class="btn btn-outline-success"
                      @click="handleApprove(participant.id)"
                      :disabled="actionLoading"
                    >
                      승인
                    </button>
                    <button
                      class="btn btn-outline-danger"
                      @click="handleReject(participant.id)"
                      :disabled="actionLoading"
                    >
                      거절
                    </button>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 수정 모달 -->
    <div v-if="showEditModal" class="modal d-block" tabindex="-1" style="background: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">그룹 수정</h5>
            <button type="button" class="btn-close" @click="showEditModal = false"></button>
          </div>
          <form @submit.prevent="handleUpdate">
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label">제목</label>
                <input type="text" class="form-control" v-model="editForm.title" required />
              </div>
              <div class="mb-3">
                <label class="form-label">총 금액</label>
                <input type="number" class="form-control" v-model.number="editForm.totalPrice" />
              </div>
              <div class="mb-3">
                <label class="form-label">최대 참여자</label>
                <input type="number" class="form-control" v-model.number="editForm.maxParticipants" />
              </div>
              <div class="mb-3">
                <label class="form-label">픽업 장소</label>
                <input type="text" class="form-control" v-model="editForm.pickupLocation" />
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="showEditModal = false">취소</button>
              <button type="submit" class="btn btn-primary" :disabled="actionLoading">저장</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getGroup, updateGroup, deleteGroup, joinGroup, cancelJoin, approveParticipant, rejectParticipant } from '../api/group'
import { getMyProfile } from '../api/user'

export default {
  name: 'GroupDetailView',

  data() {
    return {
      group: null,
      participants: [],
      currentUser: null,
      loading: true,
      actionLoading: false,
      errorMessage: '',
      successMessage: '',
      showEditModal: false,
      editForm: {
        title: '',
        totalPrice: null,
        maxParticipants: null,
        pickupLocation: ''
      }
    }
  },

  computed: {
    groupId() {
      return this.$route.params.id
    },
    isHost() {
      return this.group?.hostId === this.currentUser?.id
    },
    isParticipant() {
      return this.participants.some(p => p.userId === this.currentUser?.id)
    },
    myParticipantStatus() {
      const myParticipation = this.participants.find(p => p.userId === this.currentUser?.id)
      return myParticipation?.status || null
    },
    pricePerPerson() {
      if (this.group?.totalPrice && this.group?.maxParticipants) {
        return Math.ceil(this.group.totalPrice / this.group.maxParticipants).toLocaleString()
      }
      return '0'
    },
    progressPercent() {
      if (this.group?.maxParticipants) {
        return Math.round((this.group.currentParticipants / this.group.maxParticipants) * 100)
      }
      return 0
    },
    progressBarClass() {
      if (this.progressPercent >= 100) return 'bg-success'
      if (this.progressPercent >= 50) return 'bg-primary'
      return 'bg-warning'
    }
  },

  async mounted() {
    await this.loadData()
  },

  methods: {
    async loadData() {
      this.loading = true
      try {
        const [groupRes, profileRes] = await Promise.all([
          getGroup(this.groupId),
          getMyProfile()
        ])
        this.group = groupRes.data.data || groupRes.data
        this.participants = this.group.participants || []
        this.currentUser = profileRes.data.data
      } catch (error) {
        console.error('데이터 로드 실패:', error)
        this.errorMessage = '그룹 정보를 불러오는데 실패했습니다.'
      } finally {
        this.loading = false
      }
    },

    async handleJoin() {
      this.actionLoading = true
      this.errorMessage = ''
      try {
        await joinGroup(this.groupId)
        this.successMessage = '참여 신청이 완료되었습니다.'
        await this.loadData()
      } catch (error) {
        this.errorMessage = error.response?.data?.message || '참여 신청에 실패했습니다.'
      } finally {
        this.actionLoading = false
      }
    },

    async handleCancelJoin() {
      if (!confirm('참여를 취소하시겠습니까?')) return
      this.actionLoading = true
      this.errorMessage = ''
      try {
        await cancelJoin(this.groupId)
        this.successMessage = '참여가 취소되었습니다.'
        await this.loadData()
      } catch (error) {
        this.errorMessage = error.response?.data?.message || '참여 취소에 실패했습니다.'
      } finally {
        this.actionLoading = false
      }
    },

    async handleApprove(participantId) {
      this.actionLoading = true
      try {
        await approveParticipant(this.groupId, participantId)
        this.successMessage = '참여자를 승인했습니다.'
        await this.loadData()
      } catch (error) {
        this.errorMessage = error.response?.data?.message || '승인에 실패했습니다.'
      } finally {
        this.actionLoading = false
      }
    },

    async handleReject(participantId) {
      if (!confirm('정말 거절하시겠습니까?')) return
      this.actionLoading = true
      try {
        await rejectParticipant(this.groupId, participantId)
        this.successMessage = '참여자를 거절했습니다.'
        await this.loadData()
      } catch (error) {
        this.errorMessage = error.response?.data?.message || '거절에 실패했습니다.'
      } finally {
        this.actionLoading = false
      }
    },

    async handleUpdate() {
      this.actionLoading = true
      try {
        await updateGroup(this.groupId, this.editForm)
        this.showEditModal = false
        this.successMessage = '그룹이 수정되었습니다.'
        await this.loadData()
      } catch (error) {
        this.errorMessage = error.response?.data?.message || '수정에 실패했습니다.'
      } finally {
        this.actionLoading = false
      }
    },

    async handleDelete() {
      if (!confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return
      this.actionLoading = true
      try {
        await deleteGroup(this.groupId)
        this.$router.push('/groups')
      } catch (error) {
        this.errorMessage = error.response?.data?.message || '삭제에 실패했습니다.'
      } finally {
        this.actionLoading = false
      }
    },

    getStatusText(status) {
      const map = { RECRUITING: '모집중', FULL: '모집완료', CANCELLED: '취소됨' }
      return map[status] || status
    },

    getStatusBadgeClass(status) {
      const map = { RECRUITING: 'bg-success', FULL: 'bg-secondary', CANCELLED: 'bg-danger' }
      return map[status] || 'bg-secondary'
    },

    formatPrice(price) {
      return price?.toLocaleString() || '0'
    }
  },

  watch: {
    showEditModal(val) {
      if (val && this.group) {
        this.editForm = {
          title: this.group.title,
          totalPrice: this.group.totalPrice,
          maxParticipants: this.group.maxParticipants,
          pickupLocation: this.group.pickupLocation || ''
        }
      }
    }
  }
}
</script>
