package com.example.bob.Service;

import com.example.bob.DTO.UserDTO;
import com.example.bob.DTO.UserUpdateDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.UserHistoryEntity;
import com.example.bob.Repository.UserHistoryRepository;
import com.example.bob.Repository.UserRepository;
import com.example.bob.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.bob.Repository.NotificationRepository; // NotificationRepository 추가
import com.example.bob.Entity.NotificationEntity;
import java.util.List;

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
    private final NotificationRepository notificationRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public boolean userIdExists(String userIdLogin) {
        return userRepository.existsByUserIdLogin(userIdLogin);
    }

    public UserEntity findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

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

        if (userDTO.getProfileImageUrl() == null) {
            userDTO.setProfileImageUrl("/images/user.png");
        }
        if (userDTO.getBio() == null) {
            userDTO.setBio("소개를 작성해보세요.");
        }
        if (userDTO.getAccountCreatedAt() == null) {
            userDTO.setAccountCreatedAt(LocalDateTime.now());
        }
        if (userDTO.getRole() == null) {
            userDTO.setRole("USER");
        }

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

        // 유저 정보 업데이트
        userEntity.setUserNick(userUpdateDTO.getUserNick());
        userEntity.setUserEmail(userUpdateDTO.getUserEmail());
        userEntity.setUserBio(userUpdateDTO.getUserBio());
        userEntity.setMainLanguage(userUpdateDTO.getMainLanguage());

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
        String oldProfileImageUrl = userEntity.getProfileImageUrl();

        // 기존 이미지 삭제
        if (oldProfileImageUrl != null && !oldProfileImageUrl.equals("/images/user.png")) {
            deleteExistingProfileImage(oldProfileImageUrl);
        }

        String newProfileImageUrl = saveProfileImage(profileImage);
        userEntity.setProfileImageUrl(newProfileImageUrl);

        userRepository.save(userEntity);
    }

    private void deleteExistingProfileImage(String profileImageUrl) {
        if (profileImageUrl != null && !profileImageUrl.equals("/images/user.png")) {
            Path existingFilePath = Paths.get(uploadDir, profileImageUrl.replace("uploads/profileImages/", ""));
            try {
                Files.deleteIfExists(existingFilePath);
                log.info("✅ 기존 프로필 이미지 삭제 완료: " + existingFilePath);
            } catch (IOException e) {
                log.warn("⚠ 기존 프로필 이미지 삭제 실패: " + profileImageUrl, e);
            }
        }
    }

    // 사용자의 알림을 조회하는 메서드
    public List<NotificationEntity> getUserNotifications(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUser(user);
    }

    // 사용자가 알림을 읽었을 때 처리하는 메서드
    public void markNotificationAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);  // setRead -> setIsRead로 수정
        notificationRepository.save(notification);
    }

    // ✅ 일반 회원 목록 조회 (페이징 처리)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDTO::fromEntity);
    }

    // ✅ 특정 회원 조회
    public UserDTO getUserById(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자가 없습니다."));
        return UserDTO.fromEntity(user);
    }

    // ✅ 회원 삭제 (관리자용)
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

}
