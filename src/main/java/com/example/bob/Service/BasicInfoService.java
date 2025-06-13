package com.example.bob.Service;

import com.example.bob.DTO.BasicInfoDTO;
import com.example.bob.Entity.BasicInfo;
import com.example.bob.Repository.BasicInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicInfoService {

    private final BasicInfoRepository basicInfoRepository;

    // ✅ 기본 정보 저장
    public Long save(Long userId, BasicInfoDTO dto) {
        BasicInfo info = BasicInfo.builder()
                .userId(userId)
                .name(dto.getName())
                .birthDate(dto.getBirthDate())
                .region(dto.getRegion())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();

        return basicInfoRepository.save(info).getId();
    }

    // ✅ ID로 기본 정보 삭제
    public void deleteById(Long id) {
        basicInfoRepository.deleteById(id);
    }
}
