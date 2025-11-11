package com.ecommerce.project.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId; // 카테고리 고유 번호

    @Column(name = "category_code", nullable = false, unique = true, length = 30)
    private String categoryCode; // 카테고리 코드 (예: 0001, 00010001)

    @Column(name = "category_title", nullable = false, length = 100)
    private String categoryTitle; // 카테고리명 (예: 의류, 패딩, 가전 등)

    @Column(name = "created_at", insertable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt; // 등록일

    @Column(name = "updated_at", insertable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updatedAt; // 수정일
}
