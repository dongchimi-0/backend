package com.ecommerce.project.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "category_link")
public class CategoryLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId; // 카테고리 링크 고유 번호

    // 상품 테이블 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품 참조

    // 단순 코드 문자열 (FK 아님)
    @Column(name = "category_code", nullable = false, length = 30)
    private String categoryCode; // 카테고리 코드 (예: 0001, 00010001)
}
