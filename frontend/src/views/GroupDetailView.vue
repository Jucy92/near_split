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
              <span class="badge fs-6" :class="getStatusBadgeClass(group.groupState)">
                {{ getStatusText(group.groupState) }}
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
                  <!-- approvedCount: 호스트(1) + 승인된 참여자 수 -->
                  <span class="fw-bold">{{ approvedCount }}</span>
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
                  v-if="!isParticipant && group.groupState === 'RECRUITING'"
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
                <!-- 승인된 경우: 배지만 표시 -->
                <span v-if="myParticipantStatus === 'APPROVED'" class="badge bg-success fs-6 align-self-center">
                  참여 승인됨
                </span>
                <!--
                  거절된 경우: "재요청" 버튼 표시
                  모집 중인 상태에서만 재요청 가능
                  클릭 시 handleReJoin() 호출 → 백엔드에서 기존 REJECTED 기록 삭제 후 재신청 처리
                -->
                <button
                  v-if="myParticipantStatus === 'REJECTED' && group.groupState === 'RECRUITING'"
                  class="btn btn-outline-primary"
                  @click="handleReJoin"
                  :disabled="actionLoading"
                >
                  재요청
                </button>
                <!-- 모집 마감된 경우 거절 배지만 표시 -->
                <span v-if="myParticipantStatus === 'REJECTED' && group.groupState !== 'RECRUITING'" class="badge bg-secondary fs-6 align-self-center">
                  모집 마감
                </span>
              </template>

              <!-- 호스트일 때: 수정/삭제 버튼 -->
              <template v-if="isHost">
                <button class="btn btn-outline-primary" @click="showEditModal = true">수정</button>
                <button class="btn btn-outline-danger" @click="handleDelete" :disabled="actionLoading">삭제</button>
              </template>

              <!-- 채팅 버튼 (호스트 또는 승인된 참여자만) -->
              <!-- isHost: 방장인 경우 채팅방 입장 가능 -->
              <!-- myParticipantStatus === 'APPROVED': 참여 승인된 사용자만 채팅방 입장 가능 -->
              <router-link
                v-if="isHost || myParticipantStatus === 'APPROVED'"
                :to="`/chat/${group.id}`"
                class="btn btn-outline-secondary"
              >
                💬 채팅방
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
                :key="participant.userId"
                class="list-group-item d-flex justify-content-between align-items-center"
              >
                <div>
                  <!-- 참여자 닉네임 표시 (백엔드에서 userNickname 제공 시) -->
                  <div class="fw-bold">{{ participant.userNickname || `사용자 #${participant.userId}` }}</div>
                  <small class="text-muted">
                    {{ formatTime(participant.joinedAt) }} 신청
                  </small>
                </div>
                <div>
                  <!-- 상태에 따른 버튼 -->
                  <span v-if="participant.status === 'APPROVED'" class="badge bg-success">승인됨</span>
                  <span v-else-if="participant.status === 'REJECTED'" class="badge bg-danger">거절됨</span>
                  <div v-else class="btn-group btn-group-sm">
                    <!-- userId를 전달하여 승인/거절 처리 -->
                    <button
                      class="btn btn-outline-success"
                      @click="handleApprove(participant.userId)"
                      :disabled="actionLoading"
                    >
                      승인
                    </button>
                    <button
                      class="btn btn-outline-danger"
                      @click="handleReject(participant.userId)"
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
      // 백엔드 SplitGroupResponse의 hostUserId 필드 사용
      return this.group?.hostUserId === this.currentUser?.id
    },
    isParticipant() {
      return this.participants.some(p => p.userId === this.currentUser?.id)
    },
    myParticipantStatus() {
      const myParticipation = this.participants.find(p => p.userId === this.currentUser?.id)
      return myParticipation?.status || null
    },
    // 1인당 금액 계산 (방장 포함 N빵이므로 maxParticipants + 1)
    pricePerPerson() {
      if (this.group?.totalPrice && this.group?.maxParticipants) {
        // 방장도 금액을 나누므로 총 인원 = 참여자 + 방장(1명)
        return Math.ceil(this.group.totalPrice / (this.group.maxParticipants + 1)).toLocaleString()
      }
      return '0'
    },
    // 승인된 참여자 수 (participants 배열에서 APPROVED 상태만 카운트)
    // 백엔드에서 호스트도 participants에 APPROVED로 포함되어 있음
    approvedCount() {
      return this.participants.filter(p => p.status === 'APPROVED').length
    },
    progressPercent() {
      if (this.group?.maxParticipants) {
        // currentParticipants 대신 approvedCount 사용
        return Math.round((this.approvedCount / this.group.maxParticipants) * 100)
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
        // UserController는 ApiResponse로 감싸지 않고 UserResponse 직접 반환
        this.currentUser = profileRes.data
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

    /**
     * 거절된 참여 재요청
     * 거절(REJECTED) 상태에서 다시 참여 신청할 때 사용
     * 백엔드에서 기존 REJECTED 기록을 삭제 후 새로 PENDING 상태로 생성해야 함
     * 현재는 joinGroup API를 호출하지만, 백엔드에서 REJECTED 처리 로직이 필요함
     */
    async handleReJoin() {
      if (!confirm('다시 참여 신청하시겠습니까?')) return
      this.actionLoading = true
      this.errorMessage = ''
      try {
        // 백엔드에서 기존 REJECTED 기록을 삭제하고 새로 생성해야 함
        // 또는 별도의 reJoin API가 필요할 수 있음
        await joinGroup(this.groupId)
        this.successMessage = '참여 재신청이 완료되었습니다.'
        await this.loadData()
      } catch (error) {
        // 이미 참여 신청 기록이 있으면 에러 발생 가능
        // 백엔드에서 REJECTED 상태의 재신청 로직 추가 필요
        this.errorMessage = error.response?.data?.message || '재신청에 실패했습니다. 잠시 후 다시 시도해주세요.'
      } finally {
        this.actionLoading = false
      }
    },

    // 참여자 승인 (userId를 백엔드에 전달)
    async handleApprove(userId) {
      this.actionLoading = true
      try {
        await approveParticipant(this.groupId, userId)
        this.successMessage = '참여자를 승인했습니다.'
        await this.loadData()
      } catch (error) {
        this.errorMessage = error.response?.data?.message || '승인에 실패했습니다.'
      } finally {
        this.actionLoading = false
      }
    },

    // 참여자 거절 (userId를 백엔드에 전달)
    async handleReject(userId) {
      if (!confirm('정말 거절하시겠습니까?')) return
      this.actionLoading = true
      try {
        await rejectParticipant(this.groupId, userId)
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
    },

    // 날짜/시간 포맷 (참여 신청 시간 표시용)
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleDateString('ko-KR', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  },

  watch: {
    /**
     * URL 변경 시 데이터 다시 로드
     *
     * 케이스 1: 다른 그룹으로 이동 (/groups/5 → /groups/3)
     *   - params.id가 변경됨 → 새 그룹 데이터 로드
     *
     * 케이스 2: 같은 그룹 페이지에서 알림 클릭
     *   - params.id는 동일하지만 query.t(timestamp)가 변경됨
     *   - 참여자 목록 등 최신 데이터로 새로고침 필요
     *
     * Vue Router는 같은 컴포넌트면 재사용하므로 mounted()가 다시 호출되지 않음
     * 따라서 $route 전체를 watch해서 변경 시 수동으로 데이터 로드
     */
    '$route': {
      handler(newRoute, oldRoute) {
        // 이 컴포넌트가 표시되는 경로인지 확인 (그룹 상세 페이지)
        if (newRoute.path.startsWith('/groups/') && newRoute.params.id) {
          // 다른 그룹으로 이동하거나, 같은 그룹이지만 query가 변경된 경우
          const isIdChanged = newRoute.params.id !== oldRoute?.params?.id
          const isQueryChanged = newRoute.query?.t !== oldRoute?.query?.t

          if (isIdChanged || isQueryChanged) {
            console.log('라우트 변경 감지 - 데이터 새로고침')
            this.loadData()
          }
        }
      }
    },
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
