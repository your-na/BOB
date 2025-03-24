package com.example.bob.Service;

import com.example.bob.DTO.CompanyDTO;
import com.example.bob.DTO.CompanyUpdateDTO;
import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Entity.CompanyHistoryEntity;
import com.example.bob.Repository.CompanyHistoryRepository;
import com.example.bob.Repository.CompanyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyService{
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final CompanyHistoryRepository companyHistoryRepository;

    @Value("uploads/profileImage")
    private String uploadDir;

    public CompanyEntity findCompanyId(Long companyId) {
        return companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("Company not found"));
    }

    // 회원가입
    @Transactional
    public void save(CompanyDTO companyDTO){
        if (companyRepository.existsByCoIdLogin(companyDTO.getCoIdLogin())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        companyDTO.setCoPwd(passwordEncoder.encode(companyDTO.getCoPwd()));

        if (companyDTO.getCoImageUrl() == null){
            companyDTO.setCoImageUrl("images/user.png");
        }
        if (companyDTO.getCoBio() == null){
            companyDTO.setCoBio("소개를 작성해보세요.");
        }
        if (companyDTO.getCreatedAt() == null){
            companyDTO.setCreatedAt(LocalDateTime.now());
        }

        CompanyEntity companyEntity = CompanyEntity.toCompanyEntity(companyDTO);
        companyRepository.save(companyEntity);
    }

    // 기업 회원 정보 수정 (미완성. 기업 정보 페이지 제작 시 변경 예정)
    @Transactional
    public CompanyDTO updateCompanyInfo(CompanyUpdateDTO companyUpdateDTO, MultipartFile profileImage, Long companyId){
        CompanyEntity companyEntity = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("User not found"));

        String oldCoEmail = companyEntity.getCoEmail();
        String oldCoPhone = companyEntity.getCoPhone();
        String oldCoBio = companyEntity.getCoBio();
        String oldCoImageUrl = companyEntity.getCoImageUrl();

        // 수정 정보 업데이트
        companyEntity.setCoEmail(companyUpdateDTO.getCoEmail());
        companyEntity.setCoPhone(companyUpdateDTO.getCoPhone());
        companyEntity.setCoBio(companyUpdateDTO.getCoBio());
        companyEntity.setCoImageUrl(companyUpdateDTO.getCoImageUrl());

        companyRepository.save(companyEntity); // 바뀐 정보 저장

        // 변경 내용 히스토리 저장
        saveCompanyHistory(companyEntity,
                oldCoEmail, companyUpdateDTO.getCoEmail(),
                oldCoPhone, companyUpdateDTO.getCoPhone(),
                oldCoBio, companyUpdateDTO.getCoBio(),
                oldCoImageUrl, companyUpdateDTO.getCoImageUrl());

        // 최신 기업 정보 반황
        return CompanyDTO.toCompanyDTO(companyEntity);
    }

    private void saveCompanyHistory(CompanyEntity companyEntity,
                                    String oldEmail, String newEmail,
                                    String oldPhone, String newPhone,
                                    String oldBio, String newBio,
                                    String oldImageUrl, String newImageUrl){
        // 변경된 값 존재 시 기록
        if (!oldEmail.equals(newEmail) || !oldPhone.equals(newPhone) || !oldBio.equals(newBio) || !oldImageUrl.equals(newImageUrl)){
            CompanyHistoryEntity history = CompanyHistoryEntity.builder()
                    .companyEntity(companyEntity)
                    .coName(companyEntity.getCoName())
                    .coPwd(companyEntity.getPwd())
                    .coEmail(newEmail)
                    .coNick(companyEntity.getCoNick())
                    .coPhone(newPhone)
                    .coNum(companyEntity.getCoNum())
                    .coImageUrl(newImageUrl)
                    .coBio(newBio)
                    .updatedAt(LocalDateTime.now())
                    .build();

            companyHistoryRepository.save(history); // 변경 내용 저장
        }
    }

    // 이미지 저장은 유저 서비스에서 가져오기
}
