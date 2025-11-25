package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.dto.LoginRequestDto;
import com.ecommerce.project.backend.dto.MemberDto;
import com.ecommerce.project.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto req,
                                   HttpServletRequest request) {

        Member member = memberRepository.findByEmail(req.getEmail()).orElse(null);
        if (member == null) {
            return ResponseEntity.status(404).body("NO_USER");
        }

        if (!encoder.matches(req.getPassword(), member.getPassword())) {
            return ResponseEntity.status(401).body("WRONG_PASSWORD");
        }

        // ⭐ 세션 저장 키 통일
        HttpSession session = request.getSession(true);
        session.setAttribute("loginMember", member);

        // ⭐ 로그인 후 user 정보 반환
        return ResponseEntity.ok(MemberDto.fromEntity(member));
    }


    /** 현재 로그인 사용자 조회 */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) return ResponseEntity.status(401).build();

        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(MemberDto.fromEntity(member));
    }


    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok("LOGOUT");
    }
}
