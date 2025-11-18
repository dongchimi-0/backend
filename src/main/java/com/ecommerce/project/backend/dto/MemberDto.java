package com.ecommerce.project.backend.dto;

import lombok.*;
import com.ecommerce.project.backend.domain.Member;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    private Long id;
    private String email;
    private String password;
    private String name;
    private String phone;
    private String address;
    private String addressDetail;
    private String role;

    // Entity → DTO
    public static MemberDto fromEntity(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .password(null) // 보안상 응답에는 비밀번호를 포함시키지 않음
                .name(member.getName())
                .phone(member.getPhone())
                .address(member.getAddress())
                .addressDetail(member.getAddressDetail())
                .role(member.getRole())
                .build();
    }

    // DTO → Entity
    public Member toEntity() {
        return Member.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password) // 입력 시만 사용됨
                .name(this.name)
                .phone(this.phone)
                .address(this.address)
                .addressDetail(this.addressDetail)
                .role(this.role != null ? this.role : "USER")
                .build();
    }
}

