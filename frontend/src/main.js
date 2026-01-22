// ===========================
// Vue 애플리케이션 진입점 (Entry Point)
// ===========================
// 이 파일은 애플리케이션이 시작될 때 가장 먼저 실행됨

// Vue 3에서 createApp 함수 import
import { createApp } from 'vue'

// 최상위 컴포넌트 (App.vue) import
import App from './App.vue'

// Vue Router import
import router from './router'

// ===========================
// Bootstrap 5 CSS & JS import
// ===========================
// Bootstrap CSS: 스타일 (버튼, 카드, 그리드 등)
import 'bootstrap/dist/css/bootstrap.min.css'

// Bootstrap JS: 동적 기능 (드롭다운, 모달, 토스트 등)
// bundle.min.js: Popper.js 포함 (드롭다운 위치 계산)
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// 커스텀 CSS (필요 시 수정 가능)
import './style.css'

// ===========================
// Vue 애플리케이션 생성 및 설정
// ===========================
createApp(App)          // App.vue를 최상위 컴포넌트로 하는 Vue 앱 생성
  .use(router)          // Vue Router 플러그인 사용 (페이지 이동 기능)
  .mount('#app')        // index.html의 <div id="app"></div>에 마운트 (연결)

// ===========================
// 실행 흐름:
// ===========================
// 1. index.html 로드
// 2. main.js 실행
// 3. Vue 앱 생성 (createApp)
// 4. Router 플러그인 등록 (.use)
// 5. App.vue를 #app에 마운트 (.mount)
// 6. Router가 현재 URL 보고 해당 컴포넌트 렌더링
//    예: /login → LoginView.vue
//        /groups → GroupListView.vue
