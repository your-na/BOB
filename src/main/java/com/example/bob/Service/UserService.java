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
import org.springframework.security.core.userdetails.UserDetailsService;
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
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화
    private final UserRepository userRepository; //jpa, MySql, dependency 추가
    private final UserHistoryRepository userHistoryRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUserIdLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        return new UserDetailsImpl(userEntity);
    }

    // 회원가입 처리
    @Transactional
    public void save(UserDTO userDTO) {
        if (userRepository.existsByUserIdLogin(userDTO.getUserIdLogin())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        userDTO.setPwd(passwordEncoder.encode(userDTO.getPwd()));
        UserEntity userEntity = UserEntity.toUserEntity(userDTO);
        userRepository.save(userEntity);
    }

    @Transactional
    public UserDTO updateUserInfo(UserUpdateDTO userUpdateDTO, MultipartFile profileImage, Long userId) {
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
            updateProfileImage(profileImage, userEntity);
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

        // 최신 유저 정보를 반환
        return UserDTO.toUserDTO(userEntity);
    }

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
                    .updatedAt(LocalDateTime.now()) // 현재 날짜를 명시적으로 설정
                    .build();

            userHistoryRepository.save(history);  // 변경 내역 저장
        }
    }

    private String saveProfileImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);

            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "uploads/profileImages/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지를 저장할 수 없습니다.", e);
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