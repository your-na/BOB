package com.example.bob.security;

import com.example.bob.Service.CompanyService;
import com.example.bob.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CombinedUserDetailsService combinedUserDetailsService;

    public SecurityConfig(@Lazy CombinedUserDetailsService combinedUserDetailsService) {
        this.combinedUserDetailsService = combinedUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())  // CSRF 활성화 및 쿠키 저장
                        .ignoringRequestMatchers("/login", "/signup", "/co_signup","/profile/update", "/logout", "/teamrequest", "/teamrequest/accept", "/teamrequest/reject", "/file/project/submit")  // CSRF 예외 처리
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/","/main", "/css/**", "/js/**", "/images/**", "/static/**", "/uploads/**", "/project").permitAll()  // 모든 사용자 접근 허용
                        .requestMatchers("/login", "/sign").anonymous()  // 로그인 페이지는 익명 접근 허용
                        .requestMatchers("/profile/**", "/bw", "/postproject/**", "/myproject").authenticated()  // 인증된 사용자만 접근 가능
                        .requestMatchers("/signup", "/co_signup", "/check-nickname", "/check-username").permitAll()  // 회원가입 페이지는 익명 접근 허용
                        .anyRequest().authenticated()  // 나머지 요청은 인증된 사용자만 접근 가능
                )
                .formLogin(form -> form
                        .loginPage("/login")  // 로그인 페이지 URL
                        .loginProcessingUrl("/login")  // 로그인 처리 URL
                        .defaultSuccessUrl("/main", true)  // 로그인 성공 후 리디렉션 URL
                        .failureUrl("/login?error=true")  // 로그인 실패 시 리디렉션 URL
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
    public AuthenticationManager authenticationManager(HttpSecurity http, CustomAuthenticationProvider customAuthProvider) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(combinedUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return combinedUserDetailsService;
    }
}


