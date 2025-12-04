package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.dto.MemberAddressDto;
import com.ecommerce.project.backend.service.MemberAddressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class MemberAddressController {

    private final MemberAddressService addressService;

    /** 배송지 추가 */
    @PostMapping("/add")
    public ResponseEntity<?> add(
            @RequestBody MemberAddressDto dto,
            HttpServletRequest request) {

        Long memberId = (Long) request.getSession().getAttribute("loginMemberId");
        if (memberId == null) return ResponseEntity.status(401).body("로그인 필요");

        addressService.addAddress(memberId, dto);
        return ResponseEntity.ok("배송지 추가 성공");
    }

    /** 내 배송지 조회 */
    @GetMapping
    public ResponseEntity<?> getMyAddresses(HttpServletRequest request) {

        Long memberId = (Long) request.getSession().getAttribute("loginMemberId");
        if (memberId == null) return ResponseEntity.status(401).body("로그인 필요");

        return ResponseEntity.ok(addressService.getMyAddresses(memberId));
    }

    /** 배송지 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long memberId = (Long) request.getSession().getAttribute("loginMemberId");
        if (memberId == null) return ResponseEntity.status(401).body("로그인 필요");

        addressService.deleteAddress(id, memberId);
        return ResponseEntity.ok("삭제 완료");
    }

    /** 기본 배송지 설정 */
    @PatchMapping("/{id}/default")
    public ResponseEntity<?> setDefaultAddress(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long memberId = (Long) request.getSession().getAttribute("loginMemberId");
        if (memberId == null) return ResponseEntity.status(401).body("로그인 필요");

        addressService.setDefaultAddress(memberId, id);
        return ResponseEntity.ok("기본 배송지 설정 완료");
    }

}
