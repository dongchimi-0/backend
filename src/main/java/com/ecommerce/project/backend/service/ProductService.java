package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.config.MusinsaConfig;
import com.ecommerce.project.backend.domain.Product;
import com.ecommerce.project.backend.domain.ProductOption;
import com.ecommerce.project.backend.dto.*;
import com.ecommerce.project.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final MusinsaConfig musinsaConfig;
    private final ProductOptionRepository optionRepository;
    private final CategoryLinkRepository categoryLinkRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryTreeService categoryTreeService;

    private final ProductLikeRepository productLikeRepository;


    /**
     * 전체 상품 조회
     */
    public List<ProductDto> getAllVisibleProducts() {
        String baseUrl = musinsaConfig.getImageBaseUrl();
        return productRepository.findAllVisibleProducts()
                .stream()
                .map(p -> ProductDto.fromEntity(p, baseUrl))
                .collect(Collectors.toList());
    }

    public List<ProductListDto> getProductListLite() {
        return productRepository.findProductList();  // DTO를 직접 반환하는 빠른 쿼리
    }


    /**
     * 단일 상품 (기본 정보)
     */
    public ProductDto getProductById(Long id) {
        String baseUrl = musinsaConfig.getImageBaseUrl();
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
        return ProductDto.fromEntity(p, baseUrl);
    }

    /**
     * 검색
     */
    public List<ProductDto> searchProductsByName(String keyword) {
        String baseUrl = musinsaConfig.getImageBaseUrl();
        return productRepository.findByProductNameContaining(keyword)
                .stream()
                .map(p -> ProductDto.fromEntity(p, baseUrl))
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 조회
     */
    public List<ProductDto> getProductsByCategoryCode(String code) {
        String baseUrl = musinsaConfig.getImageBaseUrl();
        return productRepository.findByCategoryCode(code)
                .stream()
                .map(p -> ProductDto.fromEntity(p, baseUrl))
                .collect(Collectors.toList());
    }

    /**
     * 상세 정보 (옵션 + 이미지 + 카테고리)
     */
    public ProductDetailResponseDto getProductDetail(Long productId, Long memberId) {
        String baseUrl = musinsaConfig.getImageBaseUrl();

        // 상품
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + productId));

        // 옵션 조회
        List<OptionDto> options = optionRepository.findByProduct_ProductId(productId)
                .stream()
                .filter(ProductOption::getIsShow)
                .map(OptionDto::fromEntity)
                .collect(Collectors.toList());

        // 이미지
        List<String> subImages = productImageRepository
                .findByProduct_ProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());

        // 카테고리 코드
        List<String> codes = categoryLinkRepository.findByProduct_ProductId(productId)
                .stream()
                .map(c -> c.getCategoryCode())
                .collect(Collectors.toList());

        // 카테고리 경로
        String categoryPath = null;
        if (!codes.isEmpty()) {
            categoryPath = categoryTreeService.getCategoryPath(codes.get(0));
        }

        // 좋아요
        Long likeCount = productLikeRepository.countByProductId(productId);

        // 로그인 안 한 경우 userLiked는 항상 false
        boolean userLiked = false;
        if (memberId != null) {
            userLiked = productLikeRepository.existsByMemberIdAndProductId(memberId, productId);
        }
        return ProductDetailResponseDto.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .description(p.getDescription())
                .consumerPrice(p.getConsumerPrice())
                .sellPrice(p.getSellPrice())   // ⭐ 단일상품 가격
                .stock(p.getStock())
                .isOption(p.getIsOption())
                .mainImg(p.getMainImg())
                .subImages(subImages)
                .productStatus(p.getProductStatus())
                .isShow(p.getIsShow())
                .categoryPath(categoryPath)
                .categories(codes)
                .options(options)               // ⭐ C/S 옵션만 내려감
                .likeCount(likeCount)
                .userLiked(userLiked)
                .build();
    }

    public void updateProductStockAndStatus(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        // 옵션 상품
        if (Boolean.TRUE.equals(product.getIsOption())) {

            Integer totalStock = productOptionRepository.sumStockByProductId(productId);
            if (totalStock == null) totalStock = 0;

            product.setStock(totalStock);

            if (totalStock == 0) {
                product.setProductStatus(20); // 품절
            } else {
                product.setProductStatus(10); // 판매중
            }
        }
        // 단일 상품
        else {
            if (product.getStock() == 0) {
                product.setProductStatus(20); // 품절
            } else {
                product.setProductStatus(10); // 판매중
            }
        }

        productRepository.save(product);
    }


    /**
     * 상품 수정
     */
    public Product updateProduct(Long productId, ProductDto productDto) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + productId));

        // 상품 정보 수정
        existingProduct.updateProductInfo(productDto);

        // 기존 옵션 삭제 후 새 옵션 추가
        if (productDto.getIsOption() != null && productDto.getIsOption()) {
            List<ProductOptionDto> options = productDto.getOptions();

            // 기존 옵션 삭제
            productOptionRepository.deleteAllByProduct_ProductId(productId);

            options.forEach(optionDto -> {
                ProductOption option = optionDto.toEntity(productRepository);  // productId로 Product 객체 찾아서 연결
                productOptionRepository.save(option);  // 새 옵션 저장
            });
        }

        return productRepository.save(existingProduct);

    }
}

