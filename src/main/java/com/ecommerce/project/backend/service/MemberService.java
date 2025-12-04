package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.domain.MemberAddress;
import com.ecommerce.project.backend.dto.MemberAddressDto;
import com.ecommerce.project.backend.dto.MemberDto;
import com.ecommerce.project.backend.repository.MemberAddressRepository;
import com.ecommerce.project.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class  MemberService {

    private final MemberRepository memberRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 회원가입
    public MemberDto signup(MemberDto memberDto) {
        // 이메일 중복 체크
        memberRepository.findByEmail(memberDto.getEmail())
                .ifPresent(m -> { throw new IllegalArgumentException("이미 가입된 이메일입니다."); });

        // DTO → Entity 변환
        Member member = memberDto.toEntity();

        // 비밀번호 암호화
        member.setPassword(passwordEncoder.encode(memberDto.getPassword()));

        // 기본값 처리
        if (member.getName() == null) member.setName("미기입");
        if (member.getPhone() == null) member.setPhone("미기입");
        if (member.getAddress() == null) member.setAddress("미기입");

        // 회원 저장
        Member saved = memberRepository.save(member);

        return MemberDto.fromEntity(saved);
    }




    // 로그인
    public Member login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        System.out.println("==== 로그인 디버그 ====");
        System.out.println("입력 email: [" + email + "]");
        System.out.println("입력 password: [" + password + "]");
        System.out.println("DB password: [" + member.getPassword() + "]");

        boolean match = passwordEncoder.matches(password, member.getPassword());
        System.out.println("matches 결과: " + match);
        System.out.println("=====================");

        if (!match) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return member;
    }


    @Transactional
    public String resetPassword(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일이 존재하지 않습니다."));

        // 임시 비밀번호 생성
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // 암호화 후 저장
        member.setPassword(passwordEncoder.encode(tempPassword));
        memberRepository.save(member);

        return tempPassword; // Postman 응답 테스트용 (실서비스에서는 이메일로 발송)
    }

    @Transactional
    public MemberDto updateMemberInfo(Long memberId, MemberDto dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        if (dto.getName() != null) member.setName(dto.getName());
        if (dto.getPhone() != null) member.setPhone(dto.getPhone());
        if (dto.getAddress() != null) member.setAddress(dto.getAddress());
        if (dto.getAddressDetail() != null) member.setAddressDetail(dto.getAddressDetail());

        return MemberDto.fromEntity(member);
    }

    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElse(null);
    }



}

