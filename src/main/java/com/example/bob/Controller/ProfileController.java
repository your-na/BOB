package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.DTO.UserUpdateDTO;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.UserService;
import com.example.bob.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            return "redirect:/login";  // 로그인하지 않은 경우 로그인 페이지로 리디렉션
        }

        UserEntity userEntity = ((UserDetailsImpl) userDetails).getUserEntity();
        model.addAttribute("user", userEntity);

        // profileImageUrl을 제대로 설정하여 전달
        String profileImageUrl = userEntity.getProfileImageUrl(); // UserEntity에서 프로필 이미지 URL 가져오기
        model.addAttribute("profileImageUrl", profileImageUrl);

        return "profile";  // 로그인 상태면 프로필 페이지로 이동
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
                profileImageUrl = userService.saveProfileImage(profileImage); // 이미지 저장 및 URL 반환
            } catch (Exception e) {
                // 예외 처리 (이미지 저장 실패 시)
                e.printStackTrace();
                // 기본 이미지를 사용하도록 할 수 있음
                profileImageUrl = "/images/default_profile.png";
            }
        }

        // DTO 생성
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setUserNick(nickname);
        userUpdateDTO.setUserEmail(email);
        userUpdateDTO.setUserBio(bio);
        userUpdateDTO.setMainLanguage(String.join(",", languages));
        userUpdateDTO.setProfileImageUrl(profileImageUrl);

        // 사용자 정보 업데이트
        String updatedImage = userService.updateUserInfo(userUpdateDTO, profileImage, userId);

        // 타임스탬프를 추가하여 이미지 URL 갱신
        // 이미지 URL에 타임스탬프를 추가하여 캐시를 방지
        String finalImageUrl = profileImageUrl + "?timestamp=" + System.currentTimeMillis();
        redirectAttributes.addFlashAttribute("profileImageUrl", finalImageUrl);


        // 리다이렉트 시, 모델에 데이터 추가
        redirectAttributes.addFlashAttribute("profileImageUrl", finalImageUrl);

        return "redirect:/profile"; // 리다이렉트 후 새로운 프로필 페이지 표시
    }

}
