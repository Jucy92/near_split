package com.nearsplit.domain.product.service;

import com.nearsplit.common.exception.BusinessException;
import com.nearsplit.common.exception.ErrorCode;
import com.nearsplit.domain.product.dto.ProductRequest;
import com.nearsplit.domain.product.entity.Product;
import com.nearsplit.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * packageName  : com.nearsplit.domain.product.service
 * fileName     : ProductService
 * author       : user
 * date         : 2026-01-20(화)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-20(화)                user            최초 생성
 */

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;  // final 추가!

    @Transactional
    public Product createProduct(ProductRequest productRequest) {
        Product newProduct = Product.builder()
                .externalId(productRequest.getExternalId())
                .externalSource(productRequest.getExternalSource())
                .name(productRequest.getName())
                .price(productRequest.getPrice())
                .productUrl(productRequest.getProductUrl())
                .imageUrl(productRequest.getImageUrl())
                .description(productRequest.getDescription())
                .build();

        // API 정보가 들어 있으면 중복 체크
        if (newProduct.getExternalId() != null && newProduct.getExternalSource() != null) {
            Optional<Product> existing =
                    productRepository.findByExternalIdAndExternalSource(productRequest.getExternalId(),productRequest.getExternalSource());

            // 있으면 가격만 업데이트하고 반환
            if (existing.isPresent()) {
                Product findProduct = existing.get();
                if (!findProduct.getPrice().equals(newProduct.getPrice())) {
                    findProduct.setPrice(newProduct.getPrice());
                }
                log.info("외부 API 상품 업데이트: {}", findProduct.getId());
                return findProduct;
            }
        }

        Product saved = productRepository.save(newProduct);
        log.info("상품 생성 완료: {}", saved.getId());
        return saved;
    }

    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // 전체 목록 조회 (페이징)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    // 상품 검색 (이름으로)
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContaining(keyword, pageable);
    }

    // 상품 수정
    @Transactional
    public Product updateProduct(Long productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 변경된 필드만 업데이트
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getProductUrl() != null) {
            product.setProductUrl(request.getProductUrl());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        log.info("상품 수정 완료: {}", productId);
        return product;  // @Transactional이 자동으로 save 처리
    }

    // 상품 삭제
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
        log.info("상품 삭제 완료: {}", productId);
    }
}
