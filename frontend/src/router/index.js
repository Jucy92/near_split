// Vue Router: 페이지 이동을 관리하는 라우터
import { createRouter, createWebHistory } from 'vue-router'

// ===========================
// 라우트 정의 (URL과 컴포넌트 매핑)
// ===========================
const routes = [
  {
    path: '/',              // URL: http://localhost:5173/
    //name: 'Home',           // 라우트 이름 (선택)
    redirect: '/groups'     // → /groups로 자동 리다이렉트 (홈화면 역할)
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',         // URL: http://localhost:5173/login
    name: 'Login',
    // () => import(): Lazy Loading (필요할 때만 파일 로드 - 성능 향상)
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/register',      // URL: http://localhost:5173/register
    name: 'Register',
    component: () => import('../views/RegisterView.vue')
  },
  {
    path: '/groups',        // URL: http://localhost:5173/groups
    name: 'GroupList',
    component: () => import('../views/GroupListView.vue'),
    // meta: 라우트에 추가 정보 저장 (아래 라우터 가드에서 사용)
    meta: { requiresAuth: true }  // 로그인 필요한 페이지
  },
  {
    path: '/groups/:id',    // URL: http://localhost:5173/groups/123
                            // :id는 동적 파라미터 (123, 456 등 다양한 값 가능)
    name: 'GroupDetail',
    component: () => import('../views/GroupDetailView.vue'),
    meta: { requiresAuth: true }  // 로그인 필요
  },
  {
    path: '/chat/:groupId', // URL: http://localhost:5173/chat/123
    name: 'Chat',
    component: () => import('../views/ChatView.vue'),
    meta: { requiresAuth: true }  // 로그인 필요
  },
  // ===========================
  // 프로필 페이지
  // ===========================
  {
    path: '/profile',       // URL: http://localhost:5173/profile
    name: 'Profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { requiresAuth: true }  // 로그인 필요
  },
  // ===========================
  // 상품 관련 페이지
  // ===========================
  {
    path: '/products',      // URL: http://localhost:5173/products
    name: 'ProductList',
    component: () => import('../views/ProductListView.vue'),
    meta: { requiresAuth: true }  // 로그인 필요
  },
  {
    path: '/products/new',  // URL: http://localhost:5173/products/new
    name: 'ProductCreate',
    component: () => import('../views/ProductCreateView.vue'),
    meta: { requiresAuth: true }  // 로그인 필요
  },
  // ===========================
  // 결제 관련 페이지
  // ===========================
  {
    path: '/checkout/:groupId',  // URL: http://localhost:5173/checkout/123
    name: 'Checkout',
    component: () => import('../views/CheckoutView.vue'),
    meta: { requiresAuth: true }  // 로그인 필요
  },
  {
    path: '/payment/success',    // URL: http://localhost:5173/payment/success
    name: 'PaymentSuccess',
    component: () => import('../views/PaymentSuccessView.vue'),
    meta: { requiresAuth: true }  // 로그인 필요
  },
  {
    path: '/payment/fail',       // URL: http://localhost:5173/payment/fail
    name: 'PaymentFail',
    component: () => import('../views/PaymentFailView.vue'),
    meta: { requiresAuth: true }  // 로그인 필요
  }
]

// ===========================
// 라우터 인스턴스 생성
// ===========================
const router = createRouter({
  // history 모드: URL에 # 없이 깔끔하게 (예: /login, /groups)
  // hash 모드면: /#/login, /#/groups 처럼 # 붙음
  history: createWebHistory(),
  routes  // 위에서 정의한 라우트 배열
})

// ===========================
// 라우터 가드: 페이지 이동 전 실행되는 함수
// ===========================
// 용도: 로그인 체크, 권한 체크 등
router.beforeEach((to, from, next) => {
  // to: 이동하려는 페이지 정보
  // from: 현재 페이지 정보
  // next: 이동 허용/거부 함수
  //   - next(): 이동 허용
  //   - next('/login'): /login으로 리다이렉트
  //   - next(false): 이동 취소

  // 쿠키에서 accessToken 확인 (로그인 여부 체크)
  const isAuthenticated = localStorage.getItem('isLoggedIn') === 'true' //document.cookie.includes('accessToken') // 쿠키 ReadOnly 떄문에 사용 x

  // 1. 로그인이 필요한 페이지인데 로그인 안 되어 있으면?
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')  // → 로그인 페이지로 강제 이동
  }
  // 2. 이미 로그인되어 있는데 로그인 페이지 가려고 하면?
  else if (to.path === '/login' && isAuthenticated) {
    next('/groups')  // → 그룹 목록 페이지로 이동 (중복 로그인 방지)
  }
  // 3. 그 외의 경우는 정상 이동
  else {
    next()
  }
})

// 라우터 인스턴스를 export (main.js에서 사용)
export default router
