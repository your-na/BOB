package com.example.bob.Controller;

import com.example.bob.DTO.UserDTO;
import com.example.bob.DTO.UserUpdateDTO;
import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Entity.UserEntity;
import com.example.bob.Service.UserService;
import com.example.bob.security.CompanyDetailsImpl;
import com.example.bob.security.CustomUserDetails;
import com.example.bob.security.UserDetailsImpl;
import com.example.bob.Service.DashboardService;
import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.DTO.CompanyDTO;


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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import com.example.bob.DTO.CompanyUpdateDTO;
import com.example.bob.Service.CompanyService;



import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Controller
public class ProfileController {

    private final UserService userService;
    private final DashboardService dashboardService;
    private final CompanyService companyService;

    public ProfileController(UserService userService, DashboardService dashboardService, CompanyService companyService) {
        this.userService = userService;
        this.dashboardService = dashboardService;
        this.companyService = companyService;
    }


    @GetMapping("/main")
    public String mainPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            model.addAttribute("user", null);
            model.addAttribute("profileLink", "/login");
            return "main";
        }

        if (userDetails.getUserType().equals("company")) {
            CompanyEntity company = ((CompanyDetailsImpl) userDetails).getCompanyEntity();
            model.addAttribute("user", company);
            model.addAttribute("profileLink", "/company/profile");

            Map<String, Object> dashboardInfo = dashboardService.getCompanyDashboardInfo(company.getCompanyId());
            model.addAttribute("postCount", dashboardInfo.get("postCount"));
            model.addAttribute("applicantCount", dashboardInfo.get("applicantCount"));
            model.addAttribute("resumeCount", dashboardInfo.get("resumeCount"));
            model.addAttribute("recentApplicants", dashboardInfo.get("recentApplicants"));

            List<CoJobPostEntity> recentPosts = dashboardService.getRecentJobPosts(company.getCompanyId());
            model.addAttribute("recentPosts", recentPosts);

            return "main2";
        }

        UserEntity user = ((UserDetailsImpl) userDetails).getUserEntity();
        model.addAttribute("user", user);
        model.addAttribute("profileLink", "/profile");

        return "main";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        if (userDetails instanceof UserDetailsImpl user) {
            UserEntity userEntity = user.getUserEntity();
            model.addAttribute("user", userEntity);
            model.addAttribute("profileImageUrl", userEntity.getProfileImageUrl());
            return "profile";
        } else if (userDetails instanceof CompanyDetailsImpl company) {
            CompanyEntity companyEntity = company.getCompanyEntity();
            model.addAttribute("company", companyEntity);
            model.addAttribute("profileImageUrl", companyEntity.getCoImageUrl());
            return "company_profile";
        }

        return "redirect:/error";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String nickname,
                                @RequestParam String email,
                                @RequestParam String bio,
                                @RequestParam("language") List<String> languages,
                                @RequestParam("region") String region,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        if (!(userDetails instanceof UserDetailsImpl user) || user.getUserEntity().getUserId() == null) {
            System.out.println("⚠ updateProfile: userId가 null 또는 인증 정보 없음.");
            return "redirect:/login";
        }

        Long userId = user.getUserEntity().getUserId();
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setUserNick(nickname);
        userUpdateDTO.setUserEmail(email);
        userUpdateDTO.setUserBio(bio);
        userUpdateDTO.setMainLanguage(String.join(",", languages));
        userUpdateDTO.setRegion(region);

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                userService.updateProfileImage(profileImage, user.getUserEntity());
                userUpdateDTO.setProfileImageUrl(user.getUserEntity().getProfileImageUrl());
            } catch (Exception e) {
                e.printStackTrace();
                userUpdateDTO.setProfileImageUrl("/images/profile.png");
            }
        } else {
            userUpdateDTO.setProfileImageUrl(user.getUserEntity().getProfileImageUrl());
        }

        UserDTO updatedUserDTO = userService.updateUserInfo(userUpdateDTO, profileImage, userId);

        CustomUserDetails newAuth = new UserDetailsImpl(updatedUserDTO.toUserEntity());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(newAuth, newAuth.getPassword(), newAuth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String finalImageUrl = userUpdateDTO.getProfileImageUrl() + "?timestamp=" + System.currentTimeMillis();
        redirectAttributes.addFlashAttribute("profileImageUrl", finalImageUrl);
        redirectAttributes.addFlashAttribute("userNick", userUpdateDTO.getUserNick());
        redirectAttributes.addFlashAttribute("userEmail", userUpdateDTO.getUserEmail());
        redirectAttributes.addFlashAttribute("userBio", userUpdateDTO.getUserBio());
        redirectAttributes.addFlashAttribute("mainLanguage", userUpdateDTO.getMainLanguage());
        redirectAttributes.addFlashAttribute("region", userUpdateDTO.getRegion());

        return "redirect:/profile";
    }

    private final String uploadDir = "uploads/profileImages/";

    @GetMapping("/uploads/profileImages/{fileName}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
            Path filePath = Paths.get(uploadDir, decodedFileName);

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

    @GetMapping("/rating")
    public String ratingForm() {
        return "rating";
    }

    @GetMapping("/profile2")
    public String showProfilePage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails,
                                  @RequestParam(value = "success", required = false) String success) {
        if (userDetails == null) return "redirect:/login";

        if (success != null) {
            model.addAttribute("message", "수정이 완료되었습니다!");
        }

        if (userDetails instanceof CompanyDetailsImpl companyDetails) {
            Long companyId = companyDetails.getCompanyEntity().getCompanyId();

            // ✅ 여기서 최신 정보 조회
            CompanyEntity updatedCompany = companyService.findCompanyId(companyId);

            model.addAttribute("company", updatedCompany);
            model.addAttribute("profileImageUrl", updatedCompany.getCoImageUrl());

        } else if (userDetails instanceof UserDetailsImpl userDetailsImpl) {
            UserEntity user = userDetailsImpl.getUserEntity();
            model.addAttribute("user", user);
            model.addAttribute("profileImageUrl", user.getProfileImageUrl());
        }

        return "profile2";
    }


    @PostMapping("/profile/company/update")
    public String updateProfile(@ModelAttribute CompanyUpdateDTO updateDTO,
                                @AuthenticationPrincipal CompanyDetailsImpl companyDetails,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        Long companyId = companyDetails.getCompanyEntity().getCompanyId();

        CompanyDTO updatedCompanyDTO = companyService.updateCompanyInfo(updateDTO, profileImage, companyId);

        CompanyDetailsImpl newDetails = new CompanyDetailsImpl(updatedCompanyDTO.toCompanyEntity());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(newDetails, newDetails.getPassword(), newDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "redirect:/profile2?success=true";
    }





}
