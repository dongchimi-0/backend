package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.*;
import com.ecommerce.project.backend.dto.OptionDto;
import com.ecommerce.project.backend.dto.ProductDto;
import com.ecommerce.project.backend.dto.ProductImageDto;
import com.ecommerce.project.backend.dto.ProductOptionDto;
import com.ecommerce.project.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryLinkRepository categoryLinkRepository;

    /** 상품 등록 */
    public Product createProduct(ProductDto productDto) {

        // 1. 상품 유효성 검사
        if (productDto.getStock() < 0) {
            throw new IllegalArgumentException("재고는 음수가 될 수 없습니다.");
        }
        if (productDto.getSellPrice().compareTo(productDto.getConsumerPrice()) > 0) {
            throw new IllegalArgumentException("판매가는 소비자가보다 높을 수 없습니다.");
        }

        // 2. ProductDto를 사용하여 Product 객체 생성
        Product product = new Product(productDto);

        // 3. categoryCode를 받아서 Category 찾기
        String categoryCode = productDto.getCategoryCode();
        Category category = categoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 카테고리입니다."));

        // 4. 상품 옵션 처리
//        List<ProductOption> options = new ArrayList<>();
        List<ProductOption> options;
        AtomicInteger totalStock = new AtomicInteger(0);

        if (productDto.getIsOption() != null && productDto.getIsOption()) {
            options = productDto.getOptions().stream()
                    .map(optionDto -> {
                        totalStock.addAndGet(optionDto.getStock());  // AtomicInteger로 값을 더함
                        return new ProductOption(optionDto, product);  // DTO -> Entity 변환
                    })
                    .collect(Collectors.toList());
            productOptionRepository.saveAll(options);  // 옵션 일괄 저장
        }

        // 상품의 총 재고 갱신
        product.setStock(totalStock.get());  // AtomicInteger의 값을 상품의 stock에 반영
        Product savedProduct = productRepository.save(product);

        // 5. 대표 이미지 저장 (mainImg 처리)
        if (productDto.getMainImg() != null && !productDto.getMainImg().isEmpty()) {
            ProductImage mainImage = ProductImage.builder()
                    .imageUrl(productDto.getMainImg())  // mainImg URL을 저장
                    .sortOrder(1)  // 대표 이미지는 정렬순서 1로 설정
                    .product(savedProduct)  // 상품과 연결
                    .build();
            productImageRepository.save(mainImage);  // 대표 이미지 저장
        }

        // 6. 상세 이미지 처리 (subImages 처리)
        if (productDto.getSubImages() != null && !productDto.getSubImages().isEmpty()) {
            int sortOrder = 2;  // 상세 이미지의 시작 sortOrder 값 (1은 대표 이미지 사용)

            // 각 imageUrl을 ProductImage 객체로 변환하여 저장
            for (ProductImageDto productImageDto : productDto.getSubImages()) {  // subImages가 String의 리스트
                ProductImage productImage = ProductImage.builder()
                        .imageUrl(productImageDto.getImageUrl())  // imageUrl로 문자열을 전달
                        .sortOrder(sortOrder++)  // 순서대로 증가
                        .product(savedProduct)  // 상품과 연결
                        .build();
                productImageRepository.save(productImage);  // 이미지 저장
            }
        }

        // 8. 카테고리와 상품 연결 (CategoryLink 저장)
        CategoryLink categoryLink = new CategoryLink(savedProduct, category.getCategoryCode());
        categoryLinkRepository.save(categoryLink);  // 카테고리 링크 저장

        return savedProduct;  // 저장된 Product 반환
    }
}

//    /** 상품 수정 */
//    @Transactional
//    public Product updateProduct(Long productId, ProductDto productDto) {
//        // 기존 상품 조회
//        Product existingProduct = productRepository.findById(productId)
//                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + productId));
//
//        // 상품 정보 수정
//        existingProduct.updateProductInfo(productDto);
//
//        // 기존 옵션 삭제 후 새 옵션 추가
//        if (productDto.getIsOption() != null && productDto.getIsOption()) {
//            // 기존 옵션 삭제
//            productOptionRepository.deleteAllByProduct_ProductId(productId);
//
//            // 새 옵션 추가
//            for (OptionDto optionDto : productDto.getOptions()) {
//                // OptionDto에서 값을 추출하여 ProductOptionDto로 변환
//                ProductOptionDto optionDto = ProductOptionDto.builder()
//                        .optionTitle(optionDto.getOptionTitle())
//                        .optionValue(optionDto.getOptionValue())
//                        .stock(optionDto.getStock())
//                        .isShow(optionDto.getIsShow())
//                        .colorCode(optionDto.getColorCode())
//                        .sellPrice(optionDto.getSellPrice())
//                        .consumerPrice(optionDto.getConsumerPrice())
//                        .productId(existingProduct.getProductId())  // 기존 상품과 연결
//                        .build();
//
//                // ProductOptionDto에서 ProductOption 엔티티로 변환
//                ProductOption option = new ProductOption(
//                        optionDto.getOptionTitle(),
//                        optionDto.getOptionValue(),
//                        optionDto.getStock(),
//                        optionDto.getIsShow(),
//                        optionDto.getColorCode(),
//                        optionDto.getSellPrice(),
//                        optionDto.getConsumerPrice(),
//                        existingProduct // 기존 상품과 연결
//                );
//
//                // 상품 옵션 저장
//                productOptionRepository.save(option);
//            }
//        }
//
//
//        // 기존 이미지 삭제 후 새 이미지 추가
//        if (productDto.getMainImg() != null && !productDto.getMainImg().isEmpty()) {
//            // 메인 이미지 저장
//            ProductImage mainImage = new ProductImage(productDto.getMainImg(), existingProduct);
//            productImageRepository.save(mainImage);
//        }
//
//        // 서브 이미지 처리 (여러 개의 이미지 추가)
//        if (productDto.getSubImages() != null && !productDto.getSubImages().isEmpty()) {
//            // 기존 서브 이미지 삭제
//            productImageRepository.deleteAllByProduct_ProductId(productId);
//            // 서브 이미지 추가
//            int sortOrder = 2;  // 서브 이미지의 시작 sortOrder 값
//            for (String imageUrl : productDto.getSubImages()) {  // subImages가 String의 리스트
//                ProductImage productImage = ProductImage.builder()
//                        .imageUrl(imageUrl)  // imageUrl로 문자열을 전달
//                        .sortOrder(sortOrder++)  // 순서대로 증가
//                        .product(existingProduct)  // 상품과 연결
//                        .build();
//                productImageRepository.save(productImage);  // 서브 이미지 저장
//            }
//        }
//
//        // 저장된 상품 업데이트
//        return productRepository.save(existingProduct);
//    }
//
//    /** 할인가(판매가) 수정 */
//    public void updateSellPrice(Long productId, BigDecimal newSellPrice) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + productId));
//
//        // 판매가 수정
//        product.updateSellPrice(newSellPrice);
//        productRepository.save(product);
//    }
//
//    /** 재고 수정 */
//    public void updateStock(Long productId, int newStock) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + productId));
//
//        // 재고 수정
//        product.updateStock(newStock);
//        productRepository.save(product);
//    }
//
//    /** 상품 삭제 */
//    public void deleteProduct(Long productId) {
//        if (!productRepository.existsById(productId)) {
//            throw new IllegalArgumentException("삭제할 상품이 존재하지 않습니다. ID: " + productId);
//        }
//        productRepository.deleteById(productId);
//    }


//}