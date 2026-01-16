package com.nearsplit.domain.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * packageName  : com.nearsplit.domain.product.entity
 * fileName     : Product
 * author       : user
 * date         : 2026-01-15(목)
 * description   : 상품 정보(수동/API) 등록
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-15(목)                user            최초 생성
 */

@Entity
@Table(name = "products")
@Setter @Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    // 외부 API 연동용
    private String externalId;          // 외부 사이트 상품 ID
    private String externalSource;      // 외부 사이트 (쿠팡, COSTCO, MANUAL 등)

    // 상품 기본 정보
    @NotBlank
    @Column(nullable = false)
    private String name;                // 상품 이름
    @NotNull
    @Column(nullable = false)
    private BigDecimal price;           // 가격(시점에 따라 변경 가능..) => 데이터 보여주는 용
    private String imageUrl;            // 상품 이미지
    private String productUrl;          // 원본 상품 링크
    private String description;         // 상품 설명

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;



}
