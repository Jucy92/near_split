package com.nearsplit.domain.product.repository;

import com.nearsplit.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * packageName  : com.nearsplit.domain.product.repository
 * fileName     : ProductRepository
 * author       : user
 * date         : 2026-01-20(화)
 * description   :
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-20(화)                user            최초 생성
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(long id);
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByNameContaining(String name, Pageable pageable);

    Optional<Product> findByExternalIdAndExternalSource(String externalId, String externalSource);
}
