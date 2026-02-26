<!--
  파일: ChatView.vue
  설명: 실시간 채팅 페이지 (WebSocket 사용)
        - 그룹 내 실시간 메시지 송수신
        - STOMP over SockJS 프로토콜 사용
        - 연결 상태 표시 (연결됨/끊김)
        - 팝업 창으로 열기 가능 (다른 페이지 작업하면서 채팅)
        - 헤더에 그룹 타이틀 표시 (우측)

  ==================== 페이지 접근 흐름 ====================
  1. GroupDetailView 또는 GroupListView에서 "채팅방" 버튼 클릭
  2. window.open()으로 팝업 창 열기 (또는 일반 페이지 이동)
  3. ChatView 렌더링
  4. mounted()에서:
     - loadInitialData(): 기존 메시지 + 사용자 정보 로드
     - connectWebSocket(): 실시간 연결 시작
  5. 사용자가 메시지 입력 → sendMessage() → STOMP publish
  6. 백엔드가 /topic/chat/{groupId}로 브로드캐스트
  7. 구독 중인 모든 클라이언트가 메시지 수신

  ==================== 기술 스택 ====================
  - SockJS: WebSocket 폴백 라이브러리 (브라우저 호환성)
  - STOMP: 메시징 프로토콜 (구독/발행 패턴)
  - @stomp/stompjs: STOMP 클라이언트 라이브러리

  ==================== API/WebSocket 목록 ====================
  | 기능             | 타입      | 엔드포인트                    | 설명                      |
  |------------------|-----------|------------------------------|---------------------------|
  | 최근 메시지 조회 | HTTP GET  | /api/chat/{groupId}/recent   | DB에서 기존 메시지 로드    |
  | WebSocket 연결   | WebSocket | /ws                          | SockJS 연결 엔드포인트     |
  | 메시지 구독      | STOMP SUB | /topic/chat/{groupId}        | 그룹 채팅방 메시지 수신    |
  | 메시지 전송      | STOMP PUB | /app/chat/{groupId}/send     | 채팅 메시지 발송           |

  ==================== 메시지 타입 ====================
  | type   | 설명                     | 화면 표시 |
  |--------|--------------------------|-----------|
  | CHAT   | 일반 채팅 메시지         | ✅ 말풍선 |
  | NOTICE | 공지 메시지              | ✅ 시스템 |
  | SYSTEM | 시스템 메시지            | ✅ 시스템 |
  | JOIN   | 입장 메시지              | ❌ 숨김   |
  | LEAVE  | 퇴장 메시지              | ❌ 숨김   |

  ==================== 메시지 구조 (ChatMessageResponse) ====================
  {
    "id": 1,
    "groupId": 5,
    "senderId": 100,
    "senderNickname": "홍길동",
    "content": "안녕하세요",
    "type": "CHAT",
    "createdAt": "2026-01-28T10:30:00"
  }
-->
<template>
  <!--
    채팅방 전체 컨테이너
    container-fluid: Bootstrap 전체 너비 컨테이너
    py-4: padding-y (위아래 여백) 4단위
    height: 100vh: 화면 전체 높이
  -->
  <div class="container-fluid py-4" style="height: 100vh;">
    <div class="row h-100">
      <!--
        col-12: 모바일에서 전체 너비
        col-lg-8: 큰 화면에서 8칸 (12칸 중)
        offset-lg-2: 왼쪽에서 2칸 띄움 (가운데 정렬 효과)
      -->
      <div class="col-12 col-lg-8 offset-lg-2">
        <!-- 채팅 카드 (세로로 가득 채움) -->
        <div class="card shadow h-100 d-flex flex-column">

          <!-- ==================== 헤더 영역 ==================== -->
          <!--
            헤더 구성: [나가기] [연결됨] [그룹 제목...]
            - 한 줄 flex 레이아웃으로 구성
            - 버튼과 배지는 고정 크기(flex-shrink-0)로 절대 줄어들지 않음
            - 그룹 제목은 남은 공간을 모두 차지하고, 넘치면 자동으로 ... 처리
          -->
          <!--
            justify-content-between: 왼쪽 그룹(버튼+배지)과 오른쪽 제목을 양 끝에 배치
          -->
          <div class="card-header bg-white d-flex justify-content-between align-items-center">

            <!-- 왼쪽 고정 그룹: 버튼과 배지를 묶어서 고정 크기 유지 -->
            <div class="d-flex align-items-center gap-2 flex-shrink-0">
              <button
                @click="handleBackClick"
                class="btn btn-outline-secondary btn-sm"
              >
                &larr; 나가기
              </button>
              <span
                class="badge"
                :class="connected ? 'bg-success' : 'bg-danger'"
              >
                {{ connected ? '연결됨' : '연결 끊김' }}
              </span>
            </div>

            <!--
              오른쪽 그룹 제목 (우측 끝 배치 + 말줄임표)

              max-width: 60%  : 왼쪽 버튼 영역과 겹치지 않도록 최대 너비 제한
                                창이 매우 좁아져도 버튼을 침범하지 않음
              text-truncate   : 제목이 max-width를 넘으면 자동으로 ... 처리
              min-width: 0    : flex item 기본값(auto) 때문에 text-truncate가 안 먹히는 것을 방지
            -->
            <span
              class="text-primary fw-bold text-truncate"
              style="min-width: 0; max-width: 60%;"
            >
              {{ groupTitle || '로딩중...' }}
            </span>

          </div>

          <!-- ==================== 메시지 목록 영역 ==================== -->
          <!--
            ref="messageContainer": JavaScript에서 this.$refs.messageContainer로 접근 가능
            flex-grow-1: 남은 공간 모두 차지 (카드 내에서)
            overflow-auto: 내용이 넘치면 스크롤
          -->
          <div
            ref="messageContainer"
            class="card-body flex-grow-1 overflow-auto"
            style="background: #f8f9fa;"
          >
            <!--
              [권한 체크] 접근 권한 없을 때 표시하는 에러 화면
              hasPermission이 false면 (= loadInitialData에서 403 받은 경우)
              메시지 목록 대신 이 블록을 렌더링
            -->
            <div v-if="!hasPermission" class="text-center py-5">
              <p class="text-danger fs-5">{{ permissionError }}</p>
              <router-link to="/groups" class="btn btn-outline-secondary mt-2">그룹 목록으로</router-link>
            </div>

            <!-- 로딩 스피너: loading이 true일 때만 표시 -->
            <div v-else-if="loading" class="text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">로딩중...</span>
              </div>
            </div>

            <!-- 메시지 목록: 권한 있고 로딩 완료됐을 때 표시 -->
            <div v-else>
              <!-- 메시지가 없을 때 안내 문구 -->
              <div v-if="messages.length === 0" class="text-center text-muted py-5">
                아직 메시지가 없습니다. 첫 메시지를 보내보세요!
              </div>

              <!--
                v-for: messages 배열을 순회하며 각 메시지 렌더링
                :key: Vue가 각 요소를 구분하기 위한 고유 키
              -->
              <template v-for="(message, index) in messages" :key="index">
                <!--
                  날짜 구분선: 이전 메시지와 날짜가 다르면 표시
                  첫 메시지이거나 날짜가 바뀌었을 때 "2026-01-28" 형식으로 표시
                -->
                <div
                  v-if="shouldShowDateSeparator(index)"
                  class="date-separator text-center my-3"
                >
                  <span class="bg-light text-muted px-3 py-1 rounded-pill small">
                    {{ formatDate(message.createdAt) }}
                  </span>
                </div>

                <div class="mb-3" :class="getMessageAlignment(message)">
                  <!--
                    시스템 메시지 (NOTICE, SYSTEM 타입)
                    JOIN, LEAVE 타입은 제외 (입장/퇴장 메시지 표시 안 함)
                  -->
                  <div v-if="isSystemMessage(message)" class="text-center">
                    <small class="text-muted bg-white px-3 py-1 rounded">
                      {{ getSystemMessage(message) }}
                    </small>
                  </div>

                  <!-- 일반 채팅 메시지 -->
                  <div
                    v-else-if="message.type === 'CHAT'"
                    class="d-flex"
                    :class="isMyMessage(message) ? 'justify-content-end' : 'justify-content-start'"
                  >
                    <div :class="isMyMessage(message) ? 'text-end' : 'text-start'" style="max-width: 70%;">
                      <!-- 내 메시지가 아닐 때만 발신자 닉네임 표시 -->
                      <small v-if="!isMyMessage(message)" class="text-muted d-block mb-1">
                        {{ message.senderNickname || message.senderName || '익명' }}
                      </small>
                      <!--
                        메시지 말풍선
                        내 메시지: 파란 배경 + 흰 글씨 (오른쪽)
                        상대 메시지: 흰 배경 (왼쪽)
                      -->
                      <div
                        class="d-inline-block p-2 px-3 rounded-3"
                        :class="isMyMessage(message) ? 'bg-primary text-white' : 'bg-white border'"
                      >
                        {{ message.content }}
                      </div>
                      <!-- 메시지 전송 시간 -->
                      <small class="text-muted d-block mt-1">
                        {{ formatTime(message.createdAt) }}
                      </small>
                    </div>
                  </div>
                  <!-- JOIN, LEAVE 타입은 아무것도 표시하지 않음 (숨김) -->
                </div>
              </template>
            </div>
          </div>

          <!-- ==================== 메시지 입력 영역 ==================== -->
          <div class="card-footer bg-white">
            <!--
              @submit.prevent: form 제출 시 sendMessage 실행 + 페이지 새로고침 방지
            -->
            <form @submit.prevent="sendMessage" class="d-flex gap-2">
              <!--
                v-model: 입력값과 newMessage 변수 양방향 바인딩
                :disabled: connected가 false면 입력 비활성화
              -->
              <input
                type="text"
                class="form-control"
                v-model="newMessage"
                placeholder="메시지를 입력하세요..."
                :disabled="!connected"
              />
              <!--
                :disabled: 연결 안 됐거나 메시지가 비어있으면 버튼 비활성화
                newMessage.trim(): 공백만 있는 경우 방지
              -->
              <button
                type="submit"
                class="btn btn-primary"
                :disabled="!connected || !newMessage.trim()"
              >
                전송
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// ==================== 라이브러리 import ====================
// SockJS: WebSocket의 폴백(fallback) 라이브러리
// 브라우저가 WebSocket을 지원 안 하면 HTTP long-polling 등으로 대체
import SockJS from 'sockjs-client'

// STOMP: Simple Text Oriented Messaging Protocol
// WebSocket 위에서 동작하는 메시징 프로토콜 (구독/발행 패턴)
import { Client } from '@stomp/stompjs'

// API 함수들 import
import { getRecentMessages } from '../api/chat'  // 최근 메시지 조회 API
import { getMyProfile } from '../api/user'       // 내 프로필 조회 API
import { getGroup } from '../api/group'          // 그룹 정보 조회 API (타이틀 표시용)

export default {
  name: 'ChatView',

  // ==================== 컴포넌트 상태 (data) ====================
  // 반응형 데이터: 값이 변경되면 화면 자동 업데이트
  data() {
    return {
      messages: [],        // 채팅 메시지 배열
      newMessage: '',      // 입력 중인 메시지 (v-model로 바인딩)
      loading: true,       // 초기 로딩 상태
      connected: false,    // WebSocket 연결 상태
      stompClient: null,   // STOMP 클라이언트 인스턴스
      currentUser: null,   // 현재 로그인한 사용자 정보
      groupTitle: '',      // 그룹 타이틀 (헤더 우측 표시용)

      // -------------------------------------------------------
      // [권한 체크] 채팅방 접근 권한 여부
      //
      // 왜 필요한가?
      // HTTP REST(getRecentMessages)와 WebSocket(connectWebSocket)은
      // 완전히 독립적인 연결이다.
      // REST에서 FORBIDDEN이 발생해도 WebSocket 연결은 막히지 않아서,
      // 권한 없는 사용자가 실시간 메시지를 엿볼 수 있는 문제가 생겼다.
      //
      // 해결 방법:
      // loadInitialData()에서 403 에러가 오면 이 값을 false로 바꾸고,
      // mounted()에서 hasPermission이 false면 connectWebSocket()을 아예 호출하지 않는다.
      // -------------------------------------------------------
      hasPermission: true,   // true: 접근 허용 / false: 접근 차단 (403 응답 시 false로 변경됨)
      permissionError: ''    // 권한 없을 때 화면에 표시할 에러 메시지
    }
  },

  // ==================== 계산된 속성 (computed) ====================
  // 캐싱되어 의존하는 데이터가 변경될 때만 재계산
  computed: {
    // URL에서 groupId 파라미터 추출
    // 예: /chat/123 → groupId = "123"
    groupId() {
      return this.$route.params.groupId
    }
  },

  // ==================== 라이프사이클 훅 ====================

  // mounted: 컴포넌트가 DOM에 마운트된 직후 실행
  async mounted() {
    // 1. 초기 데이터 로드 (이전 메시지, 사용자 정보)
    await this.loadInitialData()

    // 2. WebSocket 연결
    // -------------------------------------------------------
    // [권한 체크] hasPermission이 true일 때만 WebSocket 연결 시작
    //
    // 왜 조건을 거는가?
    // loadInitialData()에서 403(권한 없음) 에러가 오면 hasPermission이 false가 된다.
    // 조건 없이 항상 connectWebSocket()을 호출하면,
    // REST는 막혔지만 WebSocket은 뚫려서 실시간 메시지를 엿볼 수 있는 문제가 생긴다.
    // -------------------------------------------------------
    if (this.hasPermission) {
      this.connectWebSocket()
    }
  },

  // beforeUnmount: 컴포넌트가 DOM에서 제거되기 직전 실행
  // 다른 페이지로 이동할 때 WebSocket 연결 정리
  beforeUnmount() {
    this.disconnectWebSocket()
  },

  // ==================== 메서드 ====================
  methods: {
    /**
     * 초기 데이터 로드
     * - 최근 채팅 메시지 조회
     * - 현재 사용자 프로필 조회
     */
    async loadInitialData() {
      this.loading = true  // 로딩 시작
      try {
        // Promise.all: 세 API를 병렬로 동시 호출 (성능 향상)
        const [messagesRes, profileRes, groupRes] = await Promise.all([
          getRecentMessages(this.groupId),  // GET /api/chat/{groupId}/recent
          getMyProfile(),                    // GET /api/users/me
          getGroup(this.groupId)             // GET /api/split/{groupId} (그룹 타이틀 조회용)
        ])

        // 응답에서 데이터 추출하여 상태에 저장
        // 서버 응답 구조: { success: true, data: 실제데이터 }
        // API 응답이 { data: { success, data } } 형태이므로 .data.data로 접근

        // 메시지 목록: 백엔드에서 최신순(DESC)으로 오므로 reverse해서 오래된 메시지가 위에 오도록
        const rawMessages = messagesRes.data.data || []
        this.messages = rawMessages.reverse()

        // 사용자 정보 저장 (UserController는 ApiResponse 없이 직접 반환)
        this.currentUser = profileRes.data

        // 그룹 정보에서 타이틀 추출
        // getGroup 응답 구조: { data: { data: groupInfo } } 또는 { data: groupInfo }
        const groupData = groupRes.data.data || groupRes.data
        this.groupTitle = groupData.title || `그룹 #${this.groupId}`

        // 디버그용 로그 (문제 해결 후 삭제 가능)
        console.log('=== 채팅 초기화 ===')
        console.log('현재 사용자 정보:', this.currentUser)
        console.log('현재 사용자 ID:', this.currentUser?.id)
        console.log('초기 메시지 수:', this.messages.length)
        console.log('그룹 타이틀:', this.groupTitle)

        // $nextTick: DOM 업데이트가 완료된 후 실행
        // 메시지 로드 후 스크롤을 맨 아래로 이동
        this.$nextTick(() => this.scrollToBottom())
      } catch (error) {
        // -------------------------------------------------------
        // [권한 체크] 403 응답이면 hasPermission을 false로 설정
        //
        // error.response.status: HTTP 응답 상태 코드
        //   403 = FORBIDDEN → 이 채팅방에 접근 권한 없음 (참여자도 방장도 아님)
        //   그 외 에러 (401, 500 등)는 별도 메시지로 처리
        //
        // hasPermission이 false가 되면:
        //   - mounted()에서 connectWebSocket() 호출을 건너뜀 → 실시간 수신 차단
        //   - 템플릿에서 permissionError 메시지를 화면에 표시
        // -------------------------------------------------------
        if (error.response?.status === 403) {
          this.hasPermission = false
          this.permissionError = '이 채팅방에 접근 권한이 없습니다.'
        } else {
          console.error('초기 데이터 로드 실패:', error)
        }
      } finally {
        this.loading = false  // 로딩 종료 (성공/실패 무관)
      }
    },

    /**
     * WebSocket 연결 설정
     * STOMP over SockJS 방식 사용
     */
    connectWebSocket() {
      // 1. SockJS 소켓 생성 (백엔드 WebSocket 엔드포인트)
      const socket = new SockJS('http://localhost:8080/ws')

      // 2. STOMP 클라이언트 생성 및 설정
      this.stompClient = new Client({
        // WebSocket 팩토리: SockJS 소켓 반환
        webSocketFactory: () => socket,

        // 디버그 로그 (콘솔에 STOMP 메시지 출력)
        debug: (str) => console.log('STOMP:', str),

        // 연결 끊기면 5초 후 자동 재연결 시도
        reconnectDelay: 5000,

        // 하트비트: 연결 상태 확인용 ping/pong (4초 간격)
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      })

      // 3. 연결 성공 시 콜백
      this.stompClient.onConnect = () => {
        console.log('WebSocket 연결됨')
        this.connected = true  // 연결 상태 업데이트

        // 채팅방 토픽 구독
        // /topic/chat/{groupId}로 오는 메시지를 수신
        this.stompClient.subscribe(`/topic/chat/${this.groupId}`, (message) => {
          // 수신된 메시지를 JSON 파싱
          const chatMessage = JSON.parse(message.body)

          // 디버그용 로그: 수신된 메시지와 내 메시지 여부 확인
          console.log('=== 메시지 수신 ===')
          console.log('수신된 메시지:', chatMessage)
          console.log('메시지 senderId:', chatMessage.senderId)
          console.log('내 userId:', this.currentUser?.id)
          console.log('내 메시지 여부:', chatMessage.senderId === this.currentUser?.id)

          // 메시지 배열에 추가 (화면 자동 업데이트)
          this.messages.push(chatMessage)
          // 스크롤 맨 아래로
          this.$nextTick(() => this.scrollToBottom())
        })

        // 입장 메시지는 전송하지 않음
        // 최초 그룹 참여 시에만 백엔드에서 시스템 메시지 저장
      }

      // 4. 연결 끊김 시 콜백
      this.stompClient.onDisconnect = () => {
        console.log('WebSocket 연결 끊김')
        this.connected = false
      }

      // 5. STOMP 에러 발생 시 콜백
      this.stompClient.onStompError = (frame) => {
        console.error('STOMP 에러:', frame)
        this.connected = false
      }

      // 6. 연결 시작!
      this.stompClient.activate()
    },

    /**
     * WebSocket 연결 종료
     * 페이지 이탈 시 호출됨
     */
    disconnectWebSocket() {
      // 클라이언트가 있고 연결된 상태일 때만 실행
      if (this.stompClient && this.connected) {
        // 퇴장 메시지는 전송하지 않음
        // 단순히 채팅방을 나가는 것은 알릴 필요 없음

        // STOMP 클라이언트 비활성화 (연결 종료)
        this.stompClient.deactivate()
      }
    },

    /**
     * 뒤로가기/나가기 버튼 클릭 핸들러
     * - 팝업 창으로 열린 경우: 창 닫기
     * - 일반 페이지로 접근한 경우: 그룹 상세 페이지로 이동
     *
     * window.opener: 현재 창을 연 부모 창 (팝업인 경우에만 존재)
     */
    handleBackClick() {
      // window.opener가 있으면 팝업으로 열린 것
      if (window.opener) {
        // 팝업 창 닫기
        window.close()
      } else {
        // 일반 페이지: 그룹 상세 페이지로 이동
        this.$router.push(`/groups/${this.groupId}`)
      }
    },

    /**
     * 메시지 전송
     * form 제출 시 호출됨
     */
    sendMessage() {
      // 빈 메시지이거나 연결 안 됐으면 무시
      if (!this.newMessage.trim() || !this.connected) return

      // STOMP로 메시지 발행
      // 백엔드가 받아서 /topic/chat/{groupId}로 브로드캐스트
      this.stompClient.publish({
        destination: `/app/chat/${this.groupId}/send`,
        body: JSON.stringify({
          groupId: this.groupId,
          content: this.newMessage.trim(),  // 앞뒤 공백 제거
          type: 'CHAT'  // 메시지 타입: 일반 채팅
        })
      })

      // 입력 필드 초기화
      this.newMessage = ''
    },

    /**
     * 내가 보낸 메시지인지 확인
     * @param {Object} message - 메시지 객체
     * @returns {boolean} 내 메시지면 true
     */
    isMyMessage(message) {
      return message.senderId === this.currentUser?.id
    },

    /**
     * 메시지 정렬 클래스 반환 (현재 미사용)
     * 필요시 확장 가능
     */
    getMessageAlignment(message) {
      if (message.type !== 'CHAT') return ''
      return this.isMyMessage(message) ? '' : ''
    },

    /**
     * 시스템 메시지 여부 확인
     * JOIN, LEAVE 타입은 시스템 메시지로 표시하지 않음 (숨김)
     * NOTICE, SYSTEM, JOINED 등만 표시
     * @param {Object} message - 메시지 객체
     * @returns {boolean} 시스템 메시지로 표시할지 여부
     */
    isSystemMessage(message) {
      // JOIN, LEAVE는 표시하지 않음
      const hiddenTypes = ['JOIN', 'LEAVE']
      // CHAT이 아니고, 숨김 타입도 아닌 경우에만 시스템 메시지로 표시
      return message.type !== 'CHAT' && !hiddenTypes.includes(message.type)
    },

    /**
     * 시스템 메시지 내용 반환
     * @param {Object} message - 메시지 객체
     * @returns {string} 표시할 내용
     */
    getSystemMessage(message) {
      return message.content
    },

    /**
     * 메시지가 화면에 표시되는지 확인
     * JOIN, LEAVE 타입은 표시하지 않음
     * @param {Object} message - 메시지 객체
     * @returns {boolean} 표시 여부
     */
    isVisibleMessage(message) {
      // CHAT 타입이거나 시스템 메시지(NOTICE, SYSTEM 등)만 표시
      // JOIN, LEAVE는 표시하지 않음
      const hiddenTypes = ['JOIN', 'LEAVE']
      return !hiddenTypes.includes(message.type)
    },

    /**
     * 날짜 구분선 표시 여부 확인
     * 표시되는 메시지 중 첫 메시지이거나 이전 표시 메시지와 날짜가 다르면 true
     * @param {number} index - 메시지 인덱스
     * @returns {boolean} 날짜 구분선 표시 여부
     */
    shouldShowDateSeparator(index) {
      const currentMessage = this.messages[index]

      // 현재 메시지가 표시되지 않는 타입이면 날짜 구분선도 표시하지 않음
      if (!this.isVisibleMessage(currentMessage)) return false

      // 메시지에 createdAt이 없으면 표시하지 않음
      if (!currentMessage?.createdAt) return false

      // 이전에 표시된 메시지 찾기 (JOIN, LEAVE 제외)
      let prevVisibleMessage = null
      for (let i = index - 1; i >= 0; i--) {
        if (this.isVisibleMessage(this.messages[i])) {
          prevVisibleMessage = this.messages[i]
          break
        }
      }

      // 이전에 표시된 메시지가 없으면 첫 번째 표시 메시지 → 날짜 구분선 표시
      if (!prevVisibleMessage) return true

      // 이전 표시 메시지와 날짜 비교
      const currentDate = this.getDateOnly(currentMessage.createdAt)
      const prevDate = this.getDateOnly(prevVisibleMessage.createdAt)

      return currentDate !== prevDate
    },

    /**
     * 타임스탬프에서 날짜만 추출 (비교용)
     * @param {string} timestamp - ISO 형식 날짜 문자열
     * @returns {string} "2026-01-28" 형식
     */
    getDateOnly(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toISOString().split('T')[0]  // "2026-01-28"
    },

    /**
     * 타임스탬프를 날짜 형식으로 변환 (구분선용)
     * @param {string} timestamp - ISO 형식 날짜 문자열
     * @returns {string} "2026년 1월 28일" 형식
     */
    formatDate(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },

    /**
     * 타임스탬프를 시:분 형식으로 변환
     * @param {string} timestamp - ISO 형식 날짜 문자열
     * @returns {string} "오전 10:30" 형식
     */
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })
    },

    /**
     * 메시지 컨테이너를 맨 아래로 스크롤
     * 새 메시지가 오면 최신 메시지가 보이도록
     */
    scrollToBottom() {
      // $refs: template의 ref="xxx"로 지정한 DOM 요소 접근
      const container = this.$refs.messageContainer
      if (container) {
        // scrollTop을 scrollHeight로 설정하면 맨 아래로 이동
        container.scrollTop = container.scrollHeight
      }
    }
  }
}
</script>

<style scoped>
/* scoped: 이 컴포넌트에만 적용되는 스타일 */
.card {
  /* 카드 최대 높이: 화면 높이 - 여백 */
  max-height: calc(100vh - 2rem);
}

/* 날짜 구분선 스타일 */
.date-separator {
  position: relative;
}

/* 날짜 구분선 좌우 라인 */
.date-separator::before,
.date-separator::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 30%;
  height: 1px;
  background-color: #dee2e6;
}

.date-separator::before {
  left: 5%;
}

.date-separator::after {
  right: 5%;
}
</style>
