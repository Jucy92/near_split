package com.nearsplit.domain.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * packageName  : com.nearsplit.domain.product.service
 * fileName     : ProductRequest
 * author       : user
 * date         : 2026-01-20(화)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-20(화)                user            최초 생성
 */
@Getter @Setter
public class ProductRequest {
    private String externalId;
    private String externalSource;
    @NotBlank
    private String name;
    @NotNull
    private BigDecimal price;
    private String productUrl;
    private String imageUrl;
    private String description;

}
