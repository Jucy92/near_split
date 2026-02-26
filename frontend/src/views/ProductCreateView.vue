<!--
  파일: ProductCreateView.vue
  설명: 상품 수기 등록 페이지
        - 상품 정보 입력 폼
        - 이미지 URL 입력 시 미리보기 표시
        - 등록 후 상품 목록으로 이동

  ==================== 페이지 접근 흐름 ====================
  1. ProductListView 또는 HomeView에서 "상품 등록" 버튼 클릭
  2. router가 /products/new로 이동
  3. ProductCreateView 렌더링
  4. 사용자가 상품 정보 입력
  5. "상품 등록" 버튼 클릭 → handleCreate()
  6. POST /api/products 호출
  7. 성공 시 상품 목록 페이지로 이동 (/products)

  ==================== API 목록 ====================
  | 기능       | 메서드 | 엔드포인트   | 호출 함수       |
  |------------|--------|--------------|-----------------|
  | 상품 등록  | POST   | /api/products| createProduct() |

  ==================== 백엔드 요청 구조 ====================
  POST /api/products
  Request Body (ProductRequest):
  {
    "name": "오메가3 1000mg 180정",
    "price": 35000,
    "externalSource": "MANUAL",   // COUPANG | COSTCO | MANUAL
    "externalId": "",             // 외부 사이트 상품 ID (선택)
    "productUrl": "https://...",  // 상품 페이지 URL (선택)
    "imageUrl": "https://...",    // 상품 이미지 URL (선택)
    "description": "..."          // 상품 설명 (선택)
  }

  ==================== 출처(externalSource) 옵션 ====================
  | 값      | 의미       | 설명                     |
  |---------|------------|--------------------------|
  | MANUAL  | 수기 등록  | 직접 정보 입력           |
  | COUPANG | 쿠팡       | 쿠팡 상품 정보 참조      |
  | COSTCO  | 코스트코   | 코스트코 상품 정보 참조  |
-->
<template>
  <div class="container py-4">
    <!-- 상단 헤더: 목록 링크 + 페이지 제목 -->
    <div class="d-flex align-items-center mb-4">
      <router-link to="/products" class="btn btn-outline-secondary me-3">&larr; 목록</router-link>
      <h3 class="mb-0">상품 등록 (수기)</h3>
    </div>

    <!-- 등록 폼 -->
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-6">
        <div class="card shadow">
          <div class="card-body p-4">
            <!-- 에러 메시지 영역 -->
            <div v-if="errorMessages.length > 0" class="alert alert-danger">
              <ul v-if="errorMessages.length > 1" class="mb-0 ps-3">
                <li v-for="msg in errorMessages" :key="msg">{{ msg }}</li>
              </ul>
              <span v-else>{{ errorMessages[0] }}</span>
            </div>

            <form @submit.prevent="handleCreate">
              <!-- 상품명 -->
              <div class="mb-3">
                <label for="name" class="form-label">상품명 *</label>
                <input
                  type="text"
                  class="form-control"
                  id="name"
                  v-model="form.name"
                  placeholder="예: 오메가3 1000mg 180정"
                  required
                />
              </div>

              <!-- 가격 -->
              <div class="mb-3">
                <label for="price" class="form-label">가격 (원) *</label>
                <input
                  type="number"
                  class="form-control"
                  id="price"
                  v-model.number="form.price"
                  placeholder="35000"
                  min="0"
                  required
                />
              </div>

              <!-- 출처 -->
              <div class="mb-3">
                <label for="externalSource" class="form-label">출처</label>
                <select class="form-select" id="externalSource" v-model="form.externalSource">
                  <option value="MANUAL">수기 등록</option>
                  <option value="COUPANG">쿠팡</option>
                  <option value="COSTCO">코스트코</option>
                </select>
              </div>

              <!-- 외부 ID (선택) -->
              <div class="mb-3">
                <label for="externalId" class="form-label">외부 상품 ID (선택)</label>
                <input
                  type="text"
                  class="form-control"
                  id="externalId"
                  v-model="form.externalId"
                  placeholder="쿠팡/코스트코 상품 ID"
                />
                <div class="form-text">외부 사이트의 상품 ID를 입력하세요.</div>
              </div>

              <!-- 상품 URL -->
              <div class="mb-3">
                <label for="productUrl" class="form-label">상품 URL</label>
                <input
                  type="url"
                  class="form-control"
                  id="productUrl"
                  v-model="form.productUrl"
                  placeholder="https://www.coupang.com/..."
                />
              </div>

              <!-- 이미지 URL -->
              <div class="mb-3">
                <label for="imageUrl" class="form-label">이미지 URL</label>
                <input
                  type="url"
                  class="form-control"
                  id="imageUrl"
                  v-model="form.imageUrl"
                  placeholder="https://image.coupang.com/..."
                />
                <!-- 이미지 미리보기 -->
                <div v-if="form.imageUrl" class="mt-2">
                  <img :src="form.imageUrl" alt="미리보기" class="img-thumbnail" style="max-height: 100px;" />
                </div>
              </div>

              <!-- 설명 -->
              <div class="mb-4">
                <label for="description" class="form-label">상품 설명</label>
                <textarea
                  class="form-control"
                  id="description"
                  v-model="form.description"
                  rows="3"
                  placeholder="상품에 대한 간단한 설명을 입력하세요."
                ></textarea>
              </div>

              <!-- 버튼 -->
              <div class="d-flex gap-2">
                <button type="submit" class="btn btn-success flex-grow-1" :disabled="loading">
                  <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                  {{ loading ? '등록 중...' : '상품 등록' }}
                </button>
                <router-link to="/products" class="btn btn-outline-secondary">취소</router-link>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { createProduct } from '../api/product'
import { parseError } from '../utils/errorParser'

const FIELD_LABELS = {
  name: '상품명',
  price: '가격',
  externalId: '외부 ID',
  productUrl: '상품 URL',
  imageUrl: '이미지 URL',
  description: '상품 설명',
  externalSource: '외부 소스'
}

export default {
  name: 'ProductCreateView',

  data() {
    return {
      form: {
        name: '',
        price: null,
        externalSource: 'MANUAL',
        externalId: '',
        productUrl: '',
        imageUrl: '',
        description: ''
      },
      loading: false,
      errorMessages: []
    }
  },

  methods: {
    async handleCreate() {
      this.loading = true
      this.errorMessages = []

      try {
        await createProduct(this.form)
        // 상품 목록으로 이동
        this.$router.push('/products')
      } catch (error) {
        console.error('상품 등록 실패:', error)
        this.errorMessages = parseError(error, FIELD_LABELS, '상품 등록에 실패했습니다.')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
