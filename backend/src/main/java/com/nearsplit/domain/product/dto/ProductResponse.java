package com.nearsplit.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nearsplit.domain.product.entity.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * packageName  : com.nearsplit.domain.product.dto
 * fileName     : ProductResponse
 * author       : user
 * date         : 2026-01-20(화)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-20(화)                user            최초 생성
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {
    private Long productId;

    private String externalId;
    private String externalSource;

    private String name;
    private BigDecimal price;
    private String productUrl;
    private String imageUrl;
    private String description;

    public static ProductResponse from(Product product) {
       return ProductResponse.builder()
                .productId(product.getId())
                .externalId(product.getExternalId())
                .externalSource(product.getExternalSource())
                .name(product.getName())
                .price(product.getPrice())
                .productUrl(product.getProductUrl())
                .imageUrl(product.getImageUrl())
                .description(product.getDescription())
                .build();
    }

}
