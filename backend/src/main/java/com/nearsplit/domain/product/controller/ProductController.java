package com.nearsplit.domain.product.controller;

import com.nearsplit.common.dto.ApiResponse;
import com.nearsplit.domain.product.dto.ProductRequest;
import com.nearsplit.domain.product.dto.ProductResponse;
import com.nearsplit.domain.product.entity.Product;
import com.nearsplit.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 상품 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {

        Product product = productService.createProduct(request);
        ProductResponse response = ProductResponse.from(product);

        return ResponseEntity.ok(
            ApiResponse.success(response, "상품이 등록되었습니다")
        );
    }

    // 상품 단건 조회
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable Long productId) {

        Product product = productService.getProduct(productId);
        ProductResponse response = ProductResponse.from(product);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 전체 목록 조회 (페이징)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort sort = Sort.by(Sort.Direction.valueOf(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productService.getAllProducts(pageable);
        Page<ProductResponse> responsePage = products.map(ProductResponse::from);

        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    // 상품 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products = productService.searchProducts(keyword, pageable);
        Page<ProductResponse> responsePage = products.map(ProductResponse::from);

        return ResponseEntity.ok(
            ApiResponse.success(responsePage, "검색 결과: " + products.getTotalElements() + "개")
        );
    }

    // 상품 수정
    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductRequest request) {

        Product product = productService.updateProduct(productId, request);
        ProductResponse response = ProductResponse.from(product);

        return ResponseEntity.ok(
            ApiResponse.success(response, "상품이 수정되었습니다")
        );
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId) {

        productService.deleteProduct(productId);

        return ResponseEntity.ok(
            ApiResponse.successWithMessage("상품이 삭제되었습니다")
        );
    }
}
