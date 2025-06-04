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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyService{
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final CompanyHistoryRepository companyHistoryRepository;

    @Value("uploads/profileImages")
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

    @Transactional
    public CompanyDTO updateCompanyInfo(CompanyUpdateDTO companyUpdateDTO, MultipartFile profileImage, Long companyId) {
        CompanyEntity companyEntity = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 기존 값 보존
        String oldCoEmail = companyEntity.getCoEmail();
        String oldCoPhone = companyEntity.getCoPhone();
        String oldCoBio = companyEntity.getCoBio();
        String oldCoImageUrl = companyEntity.getCoImageUrl();

        // null이 아닌 항목만 업데이트
        if (companyUpdateDTO.getCoName() != null) {
            companyEntity.setCoName(companyUpdateDTO.getCoName());
        }

        if (companyUpdateDTO.getCoNick() != null) {
            companyEntity.setCoNick(companyUpdateDTO.getCoNick());
        }

        if (companyUpdateDTO.getCoEmail() != null) {
            companyEntity.setCoEmail(companyUpdateDTO.getCoEmail());
        }

        if (companyUpdateDTO.getCoPhone() != null) {
            companyEntity.setCoPhone(companyUpdateDTO.getCoPhone());
        }

        if (companyUpdateDTO.getCoBio() != null) {
            companyEntity.setCoBio(companyUpdateDTO.getCoBio());
        }

        if (companyUpdateDTO.getCoImageUrl() != null) {
            companyEntity.setCoImageUrl(companyUpdateDTO.getCoImageUrl());
        }

        // ✅ 이미지가 업로드되었을 경우 새로 저장 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 이미지 삭제
            if (oldCoImageUrl != null && !oldCoImageUrl.equals("/images/profile.png")) {
                Path existingFilePath = Paths.get(uploadDir, oldCoImageUrl.replace("uploads/profileImages/", ""));
                try {
                    Files.deleteIfExists(existingFilePath);
                    log.info("✅ 기존 이미지 삭제: {}", existingFilePath);
                } catch (IOException e) {
                    log.warn("⚠ 기존 이미지 삭제 실패", e);
                }
            }

            try {
                String filename = UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
                Path savePath = Paths.get(uploadDir, filename);
                Files.createDirectories(savePath.getParent());
                Files.copy(profileImage.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

                companyEntity.setCoImageUrl("/profileImages/" + filename); // ✅

            } catch (IOException e) {
                e.printStackTrace();
                log.error("⚠ 이미지 저장 실패", e);
                companyEntity.setCoImageUrl("/images/profile.png"); // 실패 시 기본 이미지
            }
        }


        companyRepository.save(companyEntity); // 변경 저장

        // 변경된 항목만 기록
        saveCompanyHistory(
                companyEntity,
                oldCoEmail, companyEntity.getCoEmail(),
                oldCoPhone, companyEntity.getCoPhone(),
                oldCoBio, companyEntity.getCoBio(),
                oldCoImageUrl, companyEntity.getCoImageUrl()
        );

        return CompanyDTO.toCompanyDTO(companyEntity);
    }

    private void saveCompanyHistory(
            CompanyEntity companyEntity,
            String oldEmail, String newEmail,
            String oldPhone, String newPhone,
            String oldBio, String newBio,
            String oldImageUrl, String newImageUrl) {

        boolean isEmailChanged = (oldEmail != null && !oldEmail.equals(newEmail)) || (oldEmail == null && newEmail != null);
        boolean isPhoneChanged = (oldPhone != null && !oldPhone.equals(newPhone)) || (oldPhone == null && newPhone != null);
        boolean isBioChanged = (oldBio != null && !oldBio.equals(newBio)) || (oldBio == null && newBio != null);
        boolean isImageChanged = (oldImageUrl != null && !oldImageUrl.equals(newImageUrl)) || (oldImageUrl == null && newImageUrl != null);

        if (isEmailChanged || isPhoneChanged || isBioChanged || isImageChanged) {
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
