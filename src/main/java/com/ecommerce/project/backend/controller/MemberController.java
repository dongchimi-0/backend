package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.dto.MemberDto;
import com.ecommerce.project.backend.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody MemberDto request) {
        try {
            memberService.signup(request);
            return ResponseEntity.ok("회원가입 성공!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body("회원가입 실패: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email) {
        try {
            String tempPw = memberService.resetPassword(email);
            return ResponseEntity.ok("임시 비밀번호가 발급되었습니다: " + tempPw);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /** 내 정보 수정 */
    @PutMapping("/update")
    public ResponseEntity<?> updateMyInfo(
            @RequestBody MemberDto dto,
            HttpServletRequest request
    ) {
        Long memberId = (Long) request.getSession().getAttribute("loginMemberId");
        if (memberId == null)
            return ResponseEntity.status(401).body("로그인 필요");

        MemberDto updated = memberService.updateMemberInfo(memberId, dto);
        return ResponseEntity.ok(updated);
    }

    /** 내 정보 조회 */
    @GetMapping("/member/me")
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {

        Long memberId = (Long) request.getSession().getAttribute("loginMemberId");
        if (memberId == null) {
            return ResponseEntity.status(401).body("로그인 필요");
        }

        Member member = memberService.findById(memberId);
        if (member == null) {
            return ResponseEntity.status(404).body("회원 정보를 찾을 수 없습니다.");
        }

        MemberDto dto = MemberDto.fromEntity(member);
        return ResponseEntity.ok(dto);
    }

}
