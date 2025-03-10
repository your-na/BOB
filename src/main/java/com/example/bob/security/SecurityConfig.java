package com.example.bob.security;

import com.example.bob.Service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    private final UserService userService;  // UserService 인젝션

    public SecurityConfig(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // CSRF 활성화 및 쿠키에 토큰 저장
                        .ignoringRequestMatchers("/login", "/signup", "/profile/update", "/logout", "/bw", "/postproject/*") // 로그인과 회원가입 경로에 대해 CSRF 예외 설정
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers( "/main", "/css/**", "/js/**", "/images/**", "/static/**", "/uploads/**", "/project").permitAll() // 인증 없이 접근 가능 경로 설정
                        .requestMatchers("/login", "/sign").anonymous() // 로그인하지 않은 사용자만 접근 가능
                        .requestMatchers("/profile/**", "/bw", "/postproject/**", "/myproject").authenticated() // 프로필 페이지는 인증된 사용자만 접근 가능
                        .requestMatchers( "/signup", "/check-nickname", "/check-username").permitAll()  // 닉네임 중복 확인 요청도 인증 없이 접근 가능
                        .anyRequest().authenticated() // 나머지 요청은 인증 필요
                )
                .formLogin(form -> form
                        .loginPage("/login") // 로그인 페이지 경로
                        .loginProcessingUrl("/login") // 로그인 요청 처리 URL
                        .defaultSuccessUrl("/main", true) // 로그인 성공 후 메인 페이지로 리디렉션
                        .failureUrl("/login?error=true") // 로그인 실패 시 리디렉션
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 URL
                        .logoutSuccessUrl("/main") // 로그아웃 후 이동할 경로
                        .invalidateHttpSession(true) // ✅ 세션 무효화
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN") // ✅ CSRF 토큰 삭제 (보안 강화)
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService)  // UserDetailsService 사용
                .passwordEncoder(passwordEncoder()); // 비밀번호 암호화
        return authenticationManagerBuilder.build();
    }
}