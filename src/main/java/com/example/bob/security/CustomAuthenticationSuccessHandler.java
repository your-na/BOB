package com.example.bob.security;

import com.example.bob.Entity.UserEntity;
import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Repository.UserRepository;
import com.example.bob.Repository.CompanyRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository; // 회사 레포지토리도 주입

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();

        String redirectUrl = "/main";

        if (principal instanceof UserDetailsImpl userDetails) {
            UserEntity user = userDetails.getUserEntity();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            if ("ADMIN".equals(user.getRole())) {
                redirectUrl = "/sidebar";
            }
        } else if (principal instanceof CompanyDetailsImpl companyDetails) {
            CompanyEntity company = companyDetails.getCompanyEntity();
            company.setLastLoginAt(LocalDateTime.now());
            companyRepository.save(company);
            redirectUrl = "/main";
        }

        response.sendRedirect(redirectUrl);
    }


}
