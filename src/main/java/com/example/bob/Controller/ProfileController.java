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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";  // 로그인하지 않으면 로그인 페이지로 리디렉션
        }

        UserEntity userEntity = ((UserDetailsImpl) userDetails).getUserEntity(); // 현재 로그인한 사용자
        model.addAttribute("user", userEntity);

        // 프로필 이미지 URL을 설정
        String profileImageUrl = userEntity.getProfileImageUrl(); // UserEntity에서 프로필 이미지 URL 가져오기
        model.addAttribute("profileImageUrl", profileImageUrl);

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

        // 프로필 이미지 URL 처리
        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                profileImageUrl = userService.saveProfileImage(profileImage);
            } catch (Exception e) {
                e.printStackTrace();
                profileImageUrl = "/images/profile.png";  // 기본 이미지 경로
            }
        }

        // UserUpdateDTO 생성
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setUserNick(nickname);
        userUpdateDTO.setUserEmail(email);
        userUpdateDTO.setUserBio(bio);
        userUpdateDTO.setMainLanguage(String.join(",", languages));
        userUpdateDTO.setProfileImageUrl(profileImageUrl);

        // 사용자 정보 업데이트
        userService.updateUserInfo(userUpdateDTO, profileImage, userId);

        // 타임스탬프를 추가하여 이미지 URL 갱신
        String finalImageUrl = profileImageUrl + "?timestamp=" + System.currentTimeMillis();

        // 리다이렉트 시 프로필 이미지 URL을 전달
        redirectAttributes.addFlashAttribute("profileImageUrl", finalImageUrl);
        redirectAttributes.addFlashAttribute("user", userUpdateDTO); // 수정된 사용자 정보를 추가로 전달

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
