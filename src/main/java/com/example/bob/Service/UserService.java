package com.example.bob.Service;

import com.example.bob.DTO.UserDTO;
import com.example.bob.DTO.UserUpdateDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.UserHistoryEntity;
import com.example.bob.Repository.UserHistoryRepository;
import com.example.bob.Repository.UserRepository;
import com.example.bob.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService{

    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화
    private final UserRepository userRepository; //jpa, MySql, dependency 추가
    private final UserHistoryRepository userHistoryRepository;

    private static final String IMAGE_URL_PREFIX = "/images/profileImages/";


    @Value("${file.upload-dir}")
    private String uploadDir;

    public UserEntity getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }


    // 회원가입 처리
    public void save(UserDTO userDTO) {
        if (userRepository.existsByUserIdLogin(userDTO.getUserIdLogin())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        // 비밀번호 암호화
        userDTO.setPwd(passwordEncoder.encode(userDTO.getPwd()));

        // request -> DTO -> Entity -> Repository에서 save
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);

        // 계정 생성 날짜 설정
        userEntity.setAccountCreatedAt(LocalDateTime.now());  // 현재 시간을 계정 생성 날짜로 설정

        // 사용자 저장
        userRepository.save(userEntity);
    }

    // 로그인 검증 메서드 추가
    public boolean validateUser(String username, String password) {
        // 사용자 조회
        Optional<UserEntity> userOpt = userRepository.findByUserIdLogin(username);

        if (userOpt.isPresent()) {
            UserEntity userEntity = userOpt.get();
            // 비밀번호 검증
            return passwordEncoder.matches(password, userEntity.getPwd());
        }

        return false; // 사용자 이름이 없거나 비밀번호가 틀린 경우
    }

    // UserDetailsService 구현 (Spring Security 인증용)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userOpt = userRepository.findByUserIdLogin(username);
        if (userOpt.isPresent()) {
            UserEntity userEntity = userOpt.get();
            return new UserDetailsImpl(userEntity);  // UserDetailsImpl로 사용자 반환
        } else {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
    }

    @Transactional
    public String updateUserInfo(UserUpdateDTO userUpdateDTO, MultipartFile profileImage, Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 이전 값 저장
        String oldNick = userEntity.getUserNick();
        String oldEmail = userEntity.getUserEmail();
        String oldBio = userEntity.getUserBio();
        String oldLanguage = userEntity.getMainLanguage();
        String oldProfileImage = userEntity.getProfileImageUrl();

        // 프로필 이미지 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            updateProfileImage(profileImage, userEntity);  // 이미지 업데이트
        }

        // 유저 정보 업데이트
        userEntity.setUserNick(userUpdateDTO.getUserNick());
        userEntity.setUserEmail(userUpdateDTO.getUserEmail());
        userEntity.setUserBio(userUpdateDTO.getUserBio());
        userEntity.setMainLanguage(userUpdateDTO.getMainLanguage());

        // 프로필 이미지 URL 업데이트
        if (userUpdateDTO.getProfileImageUrl() != null) {
            userEntity.setProfileImageUrl(userUpdateDTO.getProfileImageUrl());
        }

        userRepository.save(userEntity);  // 사용자 정보 저장

        // 변경 내역 기록
        saveUserHistory(userEntity, oldNick, userUpdateDTO.getUserNick(), oldEmail, userUpdateDTO.getUserEmail(),
                oldBio, userUpdateDTO.getUserBio(), oldLanguage, userUpdateDTO.getMainLanguage(),
                oldProfileImage, userEntity.getProfileImageUrl());

        return userEntity.getProfileImageUrl();
    }

    // UserHistoryEntity 저장 메서드
    private void saveUserHistory(UserEntity userEntity, String oldNick, String newNick, String oldEmail, String newEmail,
                                 String oldBio, String newBio, String oldLanguage, String newLanguage,
                                 String oldProfileImage, String newProfileImage) {

        // 변경된 값이 있을 때만 기록
        if (!oldNick.equals(newNick) || !oldEmail.equals(newEmail) || !oldBio.equals(newBio) ||
                !oldLanguage.equals(newLanguage) || !oldProfileImage.equals(newProfileImage)) {

            UserHistoryEntity history = UserHistoryEntity.builder()
                    .userEntity(userEntity)
                    .userNick(newNick)
                    .userIdLogin(userEntity.getUserIdLogin())
                    .userName(userEntity.getUserName())
                    .pwd(userEntity.getPwd())  // 비밀번호는 업데이트 안 되므로 그대로 유지
                    .userEmail(newEmail)
                    .userPhone(userEntity.getUserPhone())
                    .sex(userEntity.getSex())
                    .mainLanguage(newLanguage)
                    .birthday(userEntity.getBirthday())
                    .profileImageUrl(newProfileImage)
                    .accountCreatedAt(userEntity.getAccountCreatedAt())
                    .userBio(newBio)
                    .updatedAt(LocalDateTime.now())
                    .build();

            userHistoryRepository.save(history);  // 변경 내역 저장
        }
    }

    public String saveProfileImage(MultipartFile file) {
        try {
            // 파일 저장 경로 생성
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // 한글을 URL 인코딩 처리
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());

            Path filePath = Paths.get(uploadDir, encodedFileName);

            // 디렉토리 확인 및 생성
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 반환할 URL (static 디렉토리 기준)
            return "uploads/profileImages/" + encodedFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Transactional
    public void updateProfileImage(MultipartFile profileImage, UserEntity userEntity) {
        deleteExistingProfileImage(userEntity.getProfileImageUrl());
        String newProfileImageUrl = saveProfileImage(profileImage);
        userEntity.setProfileImageUrl(newProfileImageUrl);
    }

    private void deleteExistingProfileImage(String profileImageUrl) {
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Path existingFilePath = Paths.get(uploadDir, profileImageUrl.replace("uploads/profileImages/", ""));
            try {
                Files.deleteIfExists(existingFilePath);
                log.info("Deleted existing profile image: " + existingFilePath);
            } catch (IOException e) {
                log.warn("Failed to delete existing profile image: " + profileImageUrl, e);
            }
        }
    }

}