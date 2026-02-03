<!--
  파일: ProductListView.vue
  설명: 상품 목록 페이지
        - 등록된 상품 목록 표시 (그리드 카드 형태)
        - 상품 검색 기능
        - 상품 삭제 기능
        - 페이지네이션

  ==================== 페이지 접근 흐름 ====================
  1. HomeView에서 "상품 목록" 버튼 클릭
  2. router가 /products로 이동
  3. ProductListView 렌더링
  4. mounted()에서 loadProducts() 호출 → GET /api/products?page=0&size=12
  5. 상품 카드 그리드로 표시

  ==================== 데이터 흐름 ====================
  mounted() → loadProducts() → GET /api/products → this.products

  검색 시:
  handleSearch() → searchProducts() → GET /api/products/search?keyword=xxx

  삭제 시:
  handleDelete(id) → deleteProduct(id) → DELETE /api/products/{id}

  ==================== API 목록 ====================
  | 기능         | 메서드 | 엔드포인트                      | 호출 함수        |
  |--------------|--------|--------------------------------|------------------|
  | 상품 목록    | GET    | /api/products?page=N&size=M    | getProducts()    |
  | 상품 검색    | GET    | /api/products/search?keyword=X | searchProducts() |
  | 상품 삭제    | DELETE | /api/products/{id}             | deleteProduct()  |

  ==================== 백엔드 응답 구조 ====================
  GET /api/products 응답 (Page<ProductResponse>):
  {
    "content": [
      {
        "id": 1,
        "name": "오메가3 1000mg",
        "price": 35000,
        "externalSource": "COUPANG",  // COUPANG | COSTCO | MANUAL
        "productUrl": "https://...",
        "imageUrl": "https://..."
      }
    ],
    "totalPages": 5,
    "totalElements": 50
  }

  ==================== 출처(externalSource) 구분 ====================
  | 값      | 의미       | 배지 색상 |
  |---------|------------|-----------|
  | COUPANG | 쿠팡       | 빨강      |
  | COSTCO  | 코스트코   | 노랑      |
  | MANUAL  | 수기등록   | 회색      |
-->
<template>
  <div class="container py-4">
    <!-- 상단 헤더: 홈 링크 + 페이지 제목 + 상품 등록 버튼 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div class="d-flex align-items-center">
        <router-link to="/home" class="btn btn-outline-secondary me-3">&larr; 홈</router-link>
        <h3 class="mb-0">상품 관리</h3>
      </div>
      <router-link to="/products/new" class="btn btn-success">+ 상품 등록</router-link>
    </div>

    <!-- 검색 -->
    <div class="row mb-4">
      <div class="col-12 col-md-6">
        <div class="input-group">
          <input
            type="text"
            class="form-control"
            placeholder="상품명 검색..."
            v-model="searchKeyword"
            @keyup.enter="handleSearch"
          />
          <button class="btn btn-outline-primary" @click="handleSearch">검색</button>
          <button v-if="isSearchMode" class="btn btn-outline-secondary" @click="clearSearch">초기화</button>
        </div>
      </div>
    </div>

    <!-- 로딩 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩중...</span>
      </div>
    </div>

    <!-- 상품 목록 -->
    <div v-else>
      <div v-if="products.length === 0" class="alert alert-info">
        {{ isSearchMode ? '검색 결과가 없습니다.' : '등록된 상품이 없습니다.' }}
      </div>

      <div v-else class="row g-3">
        <div v-for="product in products" :key="product.id" class="col-12 col-sm-6 col-md-4 col-lg-3">
          <div class="card h-100 shadow-sm product-card">
            <!-- 상품 이미지 -->
            <div class="card-img-top bg-light d-flex align-items-center justify-content-center" style="height: 150px;">
              <img
                v-if="product.imageUrl"
                :src="product.imageUrl"
                :alt="product.name"
                class="img-fluid"
                style="max-height: 150px; object-fit: contain;"
              />
              <span v-else class="text-muted">이미지 없음</span>
            </div>

            <div class="card-body">
              <h6 class="card-title text-truncate" :title="product.name">{{ product.name }}</h6>
              <p class="card-text text-primary fw-bold mb-2">{{ formatPrice(product.price) }}원</p>
              <small class="text-muted d-block mb-2">
                <span class="badge" :class="getSourceBadgeClass(product.externalSource)">
                  {{ getSourceText(product.externalSource) }}
                </span>
              </small>
            </div>

            <div class="card-footer bg-white border-top-0">
              <div class="btn-group btn-group-sm w-100">
                <a
                  v-if="product.productUrl"
                  :href="product.productUrl"
                  target="_blank"
                  class="btn btn-outline-primary"
                >
                  상품 보기
                </a>
                <button class="btn btn-outline-danger" @click="handleDelete(product.id)">삭제</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 페이지네이션 -->
      <nav v-if="totalPages > 1" class="mt-4">
        <ul class="pagination justify-content-center">
          <li class="page-item" :class="{ disabled: currentPage === 0 }">
            <button class="page-link" @click="changePage(currentPage - 1)">&laquo;</button>
          </li>
          <li
            v-for="page in totalPages"
            :key="page"
            class="page-item"
            :class="{ active: currentPage === page - 1 }"
          >
            <button class="page-link" @click="changePage(page - 1)">{{ page }}</button>
          </li>
          <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
            <button class="page-link" @click="changePage(currentPage + 1)">&raquo;</button>
          </li>
        </ul>
      </nav>
    </div>
  </div>
</template>

<script>
import { getProducts, searchProducts, deleteProduct } from '../api/product'

export default {
  name: 'ProductListView',

  data() {
    return {
      products: [],
      loading: false,
      currentPage: 0,
      totalPages: 0,
      searchKeyword: '',
      isSearchMode: false
    }
  },

  async mounted() {
    await this.loadProducts()
  },

  methods: {
    async loadProducts() {
      this.loading = true
      try {
        const response = await getProducts(this.currentPage, 12)
        const data = response.data
        this.products = data.content || []
        this.totalPages = data.totalPages || 0
        this.isSearchMode = false
      } catch (error) {
        console.error('상품 목록 로드 실패:', error)
      } finally {
        this.loading = false
      }
    },

    async handleSearch() {
      if (!this.searchKeyword.trim()) {
        await this.loadProducts()
        return
      }

      this.loading = true
      this.currentPage = 0
      try {
        const response = await searchProducts(this.searchKeyword, this.currentPage, 12)
        const data = response.data
        this.products = data.content || []
        this.totalPages = data.totalPages || 0
        this.isSearchMode = true
      } catch (error) {
        console.error('상품 검색 실패:', error)
      } finally {
        this.loading = false
      }
    },

    clearSearch() {
      this.searchKeyword = ''
      this.currentPage = 0
      this.loadProducts()
    },

    changePage(page) {
      if (page >= 0 && page < this.totalPages) {
        this.currentPage = page
        if (this.isSearchMode) {
          this.handleSearch()
        } else {
          this.loadProducts()
        }
      }
    },

    async handleDelete(productId) {
      if (!confirm('정말 삭제하시겠습니까?')) return

      try {
        await deleteProduct(productId)
        await this.loadProducts()
      } catch (error) {
        console.error('상품 삭제 실패:', error)
        alert(error.response?.data?.message || '삭제에 실패했습니다.')
      }
    },

    formatPrice(price) {
      return price?.toLocaleString() || '0'
    },

    getSourceText(source) {
      const map = {
        COUPANG: '쿠팡',
        COSTCO: '코스트코',
        MANUAL: '수기등록'
      }
      return map[source] || source || '수기등록'
    },

    getSourceBadgeClass(source) {
      const map = {
        COUPANG: 'bg-danger',
        COSTCO: 'bg-warning text-dark',
        MANUAL: 'bg-secondary'
      }
      return map[source] || 'bg-secondary'
    }
  }
}
</script>

<style scoped>
.product-card {
  transition: transform 0.2s, box-shadow 0.2s;
}
.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15) !important;
}
</style>
