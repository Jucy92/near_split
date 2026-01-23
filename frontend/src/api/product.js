// Product API 함수 모음
import apiClient from './axios'

// 상품 목록 조회 (페이징)
// GET /api/products?page=0&size=10&sortBy=createdAt&direction=DESC
export const getProducts = (page = 0, size = 10, sortBy = 'createdAt', direction = 'DESC') => {
  return apiClient.get('/products', { params: { page, size, sortBy, direction } })
}

// 상품 검색
// GET /api/products/search?keyword=...&page=0&size=10
export const searchProducts = (keyword, page = 0, size = 10) => {
  return apiClient.get('/products/search', { params: { keyword, page, size } })
}

// 상품 상세 조회
// GET /api/products/{productId}
export const getProduct = (productId) => {
  return apiClient.get(`/products/${productId}`)
}

// 상품 등록
// POST /api/products
export const createProduct = (data) => {
  return apiClient.post('/products', data)
}

// 상품 수정
// PATCH /api/products/{productId}
export const updateProduct = (productId, data) => {
  return apiClient.patch(`/products/${productId}`, data)
}

// 상품 삭제
// DELETE /api/products/{productId}
export const deleteProduct = (productId) => {
  return apiClient.delete(`/products/${productId}`)
}
