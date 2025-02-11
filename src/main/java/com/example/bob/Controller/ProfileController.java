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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public String profilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        if (!(userDetails instanceof UserDetailsImpl)) {
            return "redirect:/error";
        }

        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        Long userId = userDetailsImpl.getUserEntity().getUserId();

        if (userId == null) {
            return "redirect:/error";
        }

        UserEntity userEntity = userService.findUserById(userId);
        model.addAttribute("user", userEntity);
        model.addAttribute("profileImageUrl", userEntity.getProfileImageUrl());

        return "profile";
    }


    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String nickname,
                                @RequestParam String email,
                                @RequestParam String bio,
                                @RequestParam("language") List<String> languages,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                @AuthenticationPrincipal UserDetailsImpl userDetails,
                                RedirectAttributes redirectAttributes) {

        if (userDetails == null || userDetails.getUserEntity().getUserId() == null) {
            System.out.println("⚠ updateProfile: userId가 null 또는 인증 정보 없음.");
            return "redirect:/login"; // 인증이 없으면 로그인 페이지로 이동
        }

        Long userId = userDetails.getUserEntity().getUserId();
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setUserNick(nickname);
        userUpdateDTO.setUserEmail(email);
        userUpdateDTO.setUserBio(bio);
        userUpdateDTO.setMainLanguage(String.join(",", languages));

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                userService.updateProfileImage(profileImage, userDetails.getUserEntity());
                userUpdateDTO.setProfileImageUrl(userDetails.getUserEntity().getProfileImageUrl());
            } catch (Exception e) {
                e.printStackTrace();
                userUpdateDTO.setProfileImageUrl("/images/profile.png");
            }
        } else {
            userUpdateDTO.setProfileImageUrl(userDetails.getUserEntity().getProfileImageUrl());
        }

        // 사용자 정보 업데이트
        UserDTO updatedUserDTO = userService.updateUserInfo(userUpdateDTO, profileImage, userId);

        // SecurityContext 업데이트
        UserDetailsImpl updatedUserDetails = new UserDetailsImpl(updatedUserDTO.toUserEntity());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(updatedUserDetails, updatedUserDetails.getPassword(), updatedUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String finalImageUrl = userUpdateDTO.getProfileImageUrl() + "?timestamp=" + System.currentTimeMillis();
        redirectAttributes.addFlashAttribute("profileImageUrl", finalImageUrl);
        redirectAttributes.addFlashAttribute("userNick", userUpdateDTO.getUserNick());
        redirectAttributes.addFlashAttribute("userEmail", userUpdateDTO.getUserEmail());
        redirectAttributes.addFlashAttribute("userBio", userUpdateDTO.getUserBio());
        redirectAttributes.addFlashAttribute("mainLanguage", userUpdateDTO.getMainLanguage());

        return "redirect:/profile";
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
