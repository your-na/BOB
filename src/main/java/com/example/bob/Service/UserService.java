package com.example.bob.Service;

import com.example.bob.DTO.UserDTO;
import com.example.bob.DTO.UserUpdateDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.UserHistoryEntity;
import com.example.bob.Repository.UserHistoryRepository;
import com.example.bob.Repository.UserRepository;
import com.example.bob.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    public void updateUserInfo(UserUpdateDTO userUpdateDTO, MultipartFile profileImage, Long userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 로그 추가: userEntity가 제대로 조회되는지 확인
        log.info("Updating user with ID: " + userId);
        log.info("New Nickname: " + userUpdateDTO.getUserNick());
        log.info("New Email: " + userUpdateDTO.getUserEmail());
        log.info("New Bio: " + userUpdateDTO.getUserBio());
        log.info("New Language: " + userUpdateDTO.getMainLanguage());

        userEntity.setUserNick(userUpdateDTO.getUserNick());
        userEntity.setUserEmail(userUpdateDTO.getUserEmail());
        userEntity.setUserBio(userUpdateDTO.getUserBio());
        userEntity.setMainLanguage(userUpdateDTO.getMainLanguage());

        // 이미지 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            String profileImageUrl = saveProfileImage(profileImage); // 저장 후 URL 반환
            userEntity.setProfileImageUrl(profileImageUrl);
        }

        // 저장 전에 값 확인 로그
        log.info("Updated User Entity: " + userEntity);

        userRepository.save(userEntity);
    }


    public String saveProfileImage(MultipartFile file) {
        try {
            // 파일 저장 경로 생성
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            // 디렉토리 확인 및 생성
            if (!Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 반환할 URL (static 디렉토리 기준)
            return "/images/profileImages/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }



    private void deleteExistingProfileImage(String profileImageUrl) {
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Path existingFilePath = Paths.get(uploadDir, profileImageUrl.replace("/images/profileImages/", ""));
            try {
                Files.deleteIfExists(existingFilePath);
            } catch (IOException e) {
                log.warn("Failed to delete existing profile image: " + profileImageUrl, e);
            }
        }
    }

}