package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.DTO.UserUpdateDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.UserService;
import com.example.bob.security.UserDetailsImpl;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/main")
    public String mainPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            UserEntity userEntity = ((UserDetailsImpl) userDetails).getUserEntity();
            model.addAttribute("user", userEntity);
            model.addAttribute("profileLink", "/profile");
        } else {
            model.addAttribute("user", null);
            model.addAttribute("profileLink", "/login");
        }
        return "main";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model,
                              @ModelAttribute("user") UserUpdateDTO userUpdateDTO) {
        if (userDetails == null) {
            return "redirect:/login";  // 로그인하지 않으면 로그인 페이지로 리디렉션
        }

        UserEntity userEntity = ((UserDetailsImpl) userDetails).getUserEntity(); // 현재 로그인한 사용자
        model.addAttribute("user", userEntity);

        // 프로필 이미지 URL을 설정
        String profileImageUrl = userEntity.getProfileImageUrl(); // UserEntity에서 프로필 이미지 URL 가져오기
        model.addAttribute("profileImageUrl", profileImageUrl);

        // 수정된 사용자 정보가 있으면 이를 화면에 반영
        if (userUpdateDTO != null) {
            model.addAttribute("userNick", userUpdateDTO.getUserNick());
            model.addAttribute("userEmail", userUpdateDTO.getUserEmail());
            model.addAttribute("userBio", userUpdateDTO.getUserBio());
            model.addAttribute("mainLanguage", userUpdateDTO.getMainLanguage());
        }

        // 디버깅: userUpdateDTO가 제대로 전달되었는지 확인
        System.out.println("Updated User: " + userUpdateDTO);

        return "profile";  // 프로필 페이지로 이동
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String nickname,
                                @RequestParam String email,
                                @RequestParam String bio,
                                @RequestParam("language") List<String> languages,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                @AuthenticationPrincipal UserDetailsImpl userDetails,
                                RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUserEntity().getUserId();

        // UserUpdateDTO 생성
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setUserNick(nickname);
        userUpdateDTO.setUserEmail(email);
        userUpdateDTO.setUserBio(bio);
        userUpdateDTO.setMainLanguage(String.join(",", languages));

        // 프로필 이미지 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // 프로필 이미지 업데이트
                userService.updateProfileImage(profileImage, userDetails.getUserEntity());
                // 업데이트된 프로필 이미지 URL 가져오기
                userUpdateDTO.setProfileImageUrl(userDetails.getUserEntity().getProfileImageUrl());
            } catch (Exception e) {
                e.printStackTrace();
                // 기본 이미지 URL 설정
                userUpdateDTO.setProfileImageUrl("/images/profile.png");
            }
        } else {
            // 이미지가 없으면 기존 이미지를 그대로 유지
            userUpdateDTO.setProfileImageUrl(userDetails.getUserEntity().getProfileImageUrl());
        }

        // 디버깅: userUpdateDTO가 제대로 설정되었는지 확인
        System.out.println("Updated User: " + userUpdateDTO);  // userUpdateDTO의 상태 출력

        // 사용자 정보 업데이트
        UserDTO updatedUserDTO = userService.updateUserInfo(userUpdateDTO, profileImage, userId);

        // 업데이트된 사용자 정보를 UserDetailsImpl에 반영
        userDetails.updateUserEntity(updatedUserDTO.toUserEntity());

        // 타임스탬프를 추가하여 이미지 URL 갱신
        String finalImageUrl = userUpdateDTO.getProfileImageUrl() + "?timestamp=" + System.currentTimeMillis();

        // 리다이렉트 시 프로필 이미지 URL을 전달
        redirectAttributes.addFlashAttribute("profileImageUrl", finalImageUrl);
        redirectAttributes.addFlashAttribute("userNick", userUpdateDTO.getUserNick());
        redirectAttributes.addFlashAttribute("userEmail", userUpdateDTO.getUserEmail());
        redirectAttributes.addFlashAttribute("userBio", userUpdateDTO.getUserBio());
        redirectAttributes.addFlashAttribute("mainLanguage", userUpdateDTO.getMainLanguage());

        return "redirect:/profile";  // 리다이렉트 후 새로운 프로필 페이지 표시
    }

    private final String uploadDir = "uploads/profileImages/";  // 파일이 저장된 경로

    @GetMapping("/uploads/profileImages/{fileName}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            // URL 디코딩 처리
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());

            // 파일 경로 생성
            Path filePath = Paths.get(uploadDir, decodedFileName);

            // 파일이 존재하는지 확인
            if (Files.exists(filePath)) {
                Resource file = new UrlResource(filePath.toUri());
                return ResponseEntity.ok().body(file);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
