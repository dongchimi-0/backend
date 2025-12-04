package com.ecommerce.project.backend.service;

import com.ecommerce.project.backend.domain.Member;
import com.ecommerce.project.backend.domain.MemberAddress;
import com.ecommerce.project.backend.dto.MemberAddressDto;
import com.ecommerce.project.backend.repository.MemberAddressRepository;
import com.ecommerce.project.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberAddressService {

    private final MemberAddressRepository addressRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void addAddress(Long memberId, MemberAddressDto dto) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        if (dto.getIsDefault()) {
            List<MemberAddress> list = addressRepository.findByMemberId(memberId);
            list.forEach(addr -> addr.setIsDefault(false));
        }

        MemberAddress address = MemberAddress.builder()
                .member(member)
                .name(dto.getName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .detail(dto.getDetail())
                .isDefault(dto.getIsDefault())
                .build();

        addressRepository.save(address);
    }

    public List<MemberAddressDto> getMyAddresses(Long memberId) {
        return addressRepository.findByMemberId(memberId).stream()
                .map(addr -> MemberAddressDto.builder()
                        .id(addr.getId())
                        .name(addr.getName())
                        .phone(addr.getPhone())
                        .address(addr.getAddress())
                        .detail(addr.getDetail())
                        .isDefault(addr.getIsDefault())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteAddress(Long addressId, Long memberId) {
        MemberAddress addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("배송지 없음"));

        if (!addr.getMember().getId().equals(memberId))
            throw new RuntimeException("권한 없음");

        addressRepository.delete(addr);
    }

    @Transactional
    public void setDefaultAddress(Long memberId, Long addressId) {

        // 모든 배송지 기본값 해제
        List<MemberAddress> list = addressRepository.findByMemberId(memberId);
        list.forEach(addr -> addr.setIsDefault(false));

        // 선택한 주소 기본값 true로 변경
        MemberAddress target = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("배송지 없음"));

        if (!target.getMember().getId().equals(memberId))
            throw new RuntimeException("권한 없음");

        target.setIsDefault(true);
    }

}
