<template>
  <div class="container py-4">
    <!-- 상단 헤더 -->
    <div class="d-flex align-items-center mb-4">
      <router-link to="/products" class="btn btn-outline-secondary me-3">&larr; 목록</router-link>
      <h3 class="mb-0">상품 등록 (수기)</h3>
    </div>

    <!-- 등록 폼 -->
    <div class="row justify-content-center">
      <div class="col-12 col-md-8 col-lg-6">
        <div class="card shadow">
          <div class="card-body p-4">
            <!-- 에러 메시지 -->
            <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

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
      errorMessage: ''
    }
  },

  methods: {
    async handleCreate() {
      this.loading = true
      this.errorMessage = ''

      try {
        await createProduct(this.form)
        // 상품 목록으로 이동
        this.$router.push('/products')
      } catch (error) {
        console.error('상품 등록 실패:', error)
        if (error.response?.data?.message) {
          this.errorMessage = error.response.data.message
        } else {
          this.errorMessage = '상품 등록에 실패했습니다.'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
