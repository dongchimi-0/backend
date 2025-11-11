package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 회원가입
    public Member signup(Member member) {
        // 이메일 중복 체크
        memberRepository.findByEmail(member.getEmail())
                .ifPresent(m -> { throw new IllegalArgumentException("이미 가입된 이메일입니다."); });

        // 비밀번호 암호화
        member.setPassword(passwordEncoder.encode(member.getPassword()));

        // ✅ 이름, 전화번호, 주소가 비어 있으면 기본값 처리 (선택)
        if (member.getName() == null) member.setName("미기입");
        if (member.getPhone() == null) member.setPhone("미기입");
        if (member.getAddress() == null) member.setAddress("미기입");

        // 저장
        return memberRepository.save(member);
    }
}

