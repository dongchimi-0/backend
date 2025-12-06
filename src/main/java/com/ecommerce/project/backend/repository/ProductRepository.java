package com.ecommerce.project.backend.repository;

import com.ecommerce.project.backend.domain.Product;
import com.ecommerce.project.backend.domain.ProductOption;
import com.ecommerce.project.backend.dto.ProductListDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 목록 전용 DTO 조회
    @Query("""
        select new com.ecommerce.project.backend.dto.ProductListDto(
            p.productId,
            p.productName,
            p.sellPrice,
            p.consumerPrice,
            p.mainImg
        )
        from Product p
        where p.isShow = true
        order by p.productId asc
    """)
    List<ProductListDto> findProductList();

    //관리자용 전체 조회
    List<Product> findAll();

    // 상품명 검색
    List<Product> findByProductNameContaining(String keyword);

    // 노출 중인 상품만
    List<Product> findByIsShowTrue();

    // 상품 상태별 조회
    List<Product> findByProductStatus(int productStatus);

    // 옵션 상품 여부로 조회
    List<Product> findByIsOption(boolean isOption);

    // 가격 범위 조회
    List<Product> findBySellPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // 카테고리 코드로 상품 조회 (category_link 조인)
    @Query("""
        SELECT p FROM Product p
        JOIN p.categoryLinks cl
        WHERE cl.categoryCode = :code
    """)
    List<Product> findByCategoryCode(@Param("code") String code);

    // is_show가 true(1)인 상품만 조회
    @Query("SELECT p FROM Product p WHERE p.isShow = true")
    List<Product> findAllVisibleProducts();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.productId = :id")
    Optional<Product> findProductWithImages(@Param("id") Long id);

    // 상품 ID로 옵션 목록 조회
    List<ProductOption> findByProductId(Long productId);

}
