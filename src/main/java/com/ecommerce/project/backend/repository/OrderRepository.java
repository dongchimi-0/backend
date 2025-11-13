package com.ecommerce.project.backend.repository;

import com.ecommerce.project.backend.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMember_Id(Long memberId);
}