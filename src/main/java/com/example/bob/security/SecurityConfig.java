package com.example.bob.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;




// ✅ 아래 import 추가
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CombinedUserDetailsService combinedUserDetailsService;

    // @Lazy 추가하여 순환 의존성 해결
    @Autowired
    @Lazy  // CustomAuthenticationProvider에 @Lazy 추가
    private CustomAuthenticationProvider customAuthProvider;  // CustomAuthenticationProvider 주입

    // Constructor 의존성 주입 방식
    public SecurityConfig(CombinedUserDetailsService combinedUserDetailsService) {
        this.combinedUserDetailsService = combinedUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())  // CSRF 활성화 및 쿠키 저장
                        .ignoringRequestMatchers("/login", "/signup", "/co_signup","/profile/update", "/logout", "/teamrequest", "/teamrequest/accept", "/teamrequest/reject", "/file/project/submit", "/api/todos/**")  // CSRF 예외 처리
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/","/main", "/css/**", "/js/**", "/images/**", "/static/**", "/uploads/**", "/project", "/contest").permitAll()  // 모든 사용자 접근 허용
                        .requestMatchers("/login", "/sign").anonymous()  // 로그인 페이지는 익명 접근 허용
                        .requestMatchers("/profile/**", "/bw", "/postproject/**", "/myproject", "/api/todos").authenticated()// 인증된 사용자만 접근 가능
                        .requestMatchers("/signup", "/co_signup", "/check-nickname", "/check-username",  "/api/my-projects").permitAll()  // 회원가입 페이지는 익명 접근 허용
                        .requestMatchers("/admin/**", "/sidebar").hasAuthority("ADMIN")
                        .requestMatchers("/contest/create", "/contest/submit").hasAnyAuthority("ADMIN", "COMPANY")
                        .anyRequest().authenticated()  // 나머지 요청은 인증된 사용자만 접근 가능
                )
                .formLogin(form -> form
                        .loginPage("/login")  // 로그인 페이지 URL
                        .loginProcessingUrl("/login")  // 로그인 처리 URL
                        .successHandler((request, response, authentication) -> {
                            Object principal = authentication.getPrincipal();
                            String redirectUrl = "/main";

                            // 관리자면 관리자 페이지로 이동
                            if (principal instanceof com.example.bob.security.UserDetailsImpl user) {
                                if ("ADMIN".equals(user.getUserEntity().getRole())) {
                                    redirectUrl = "/sidebar";
                                }
                            }

                            response.sendRedirect(redirectUrl);
                        })
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")  // 로그아웃 URL
                        .logoutSuccessUrl("/main")  // 로그아웃 성공 후 이동할 URL
                        .invalidateHttpSession(true)  // 세션 무효화
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")  // 로그아웃 후 쿠키 삭제
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)  // 최대 세션 수 1
                        .expiredUrl("/login")  // 세션 만료 시 로그인 페이지로 리디렉션
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
        authenticationManagerBuilder
                .userDetailsService(combinedUserDetailsService)  // CombinedUserDetailsService 사용
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return combinedUserDetailsService;
    }

    // ✅ 추가된 부분: Spring Security Dialect 등록
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:8888")); // 프론트 주소!
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // 세션 쿠키 허용 시 필요

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 요청에 적용
        return source;
    }

}
