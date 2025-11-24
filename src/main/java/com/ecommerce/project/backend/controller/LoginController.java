//package com.ecommerce.project.backend.controller;
//
//import com.ecommerce.project.backend.domain.Member;
//import com.ecommerce.project.backend.dto.LoginRequestDto;
//import com.ecommerce.project.backend.repository.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.servlet.http.HttpSession;
//
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api")
//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
//public class LoginController {
//
//    private final MemberRepository memberRepository;
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequestDto request, HttpSession session) {
//
//        String email = request.getEmail();
//        String pw = request.getPassword();
//
//        System.out.println("\n===== 로그인 요청 =====");
//        System.out.println("입력 이메일: " + email);
//        System.out.println("입력 PW(plain): " + pw);
//
//        Optional<Member> memberOpt = memberRepository.findByEmail(email);
//        System.out.println("DB 조회 결과: " + memberOpt);
//
//        if (memberOpt.isEmpty()) {
//            System.out.println("⚠ 이메일 존재 X");
//            return ResponseEntity.status(404).body("존재하지 않는 사용자입니다.");
//        }
//
//        Member member = memberOpt.get();
//
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//
//        System.out.println("DB 저장 PW: " + member.getPassword());
//        System.out.println("비밀번호 match 여부: " + encoder.matches(pw, member.getPassword()));
//        System.out.println("유저 role: " + member.getRole());
//
//        if (!encoder.matches(pw, member.getPassword())) {
//            return ResponseEntity.status(401).body("비밀번호가 일치하지 않습니다.");
//        }
//
//        session.setAttribute("user", member);
//        session.setAttribute("memberId", member.getId());
//
//        return ResponseEntity.ok(
//                Map.of(
//                        "id", member.getId(),
//                        "email", member.getEmail(),
//                        "name", member.getName(),
//                        "role", member.getRole().toUpperCase()
//                )
//        );
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<String> logout(HttpSession session) {
//        session.invalidate();
//        return ResponseEntity.ok("로그아웃 성공");
//    }
//}


package com.ecommerce.project.backend.controller;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.dto.LoginRequestDto;
import com.ecommerce.project.backend.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LoginController {

    private final MemberRepository memberRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request, HttpSession session) {

        Optional<Member> memberOpt = memberRepository.findByEmail(request.getEmail());
        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(404).body("존재하지 않는 사용자입니다.");
        }

        Member member = memberOpt.get();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(request.getPassword(), member.getPassword())) {
            return ResponseEntity.status(401).body("비밀번호가 일치하지 않습니다.");
        }

        // ⭐ 세션에 memberId 저장
        session.setAttribute("memberId", member.getId());

        return ResponseEntity.ok(
                Map.of(
                        "id", member.getId(),
                        "email", member.getEmail(),
                        "name", member.getName(),
                        "role", member.getRole()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공");
    }
}
