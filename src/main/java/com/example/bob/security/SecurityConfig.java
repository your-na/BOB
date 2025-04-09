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

    @Autowired
    @Lazy  // 순환 의존성 방지
    private CustomAuthenticationProvider customAuthProvider;

    public SecurityConfig(CombinedUserDetailsService combinedUserDetailsService) {
        this.combinedUserDetailsService = combinedUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/login", "/signup", "/co_signup", "/profile/update", "/logout",
                                "/teamrequest", "/teamrequest/accept", "/teamrequest/reject",
                                "/file/project/submit", "/api/todos/**", "/api/notifications/delete-all"
                        )
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()) // ✅ iframe 허용 설정
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/main", "/css/**", "/js/**", "/images/**", "/static/**", "/uploads/**", "/project", "/project/api","/contest").permitAll()
                        .requestMatchers("/login", "/sign").anonymous()
                        .requestMatchers("/profile/**", "/bw", "/postproject/**", "/myproject", "/api/todos", "/api/resumes").authenticated()
                        .requestMatchers("/signup", "/co_signup", "/check-nickname", "/check-username", "/api/my-projects").permitAll()
                        .requestMatchers("/comhome/**", "/comhost").hasAuthority("COMPANY")
                        .requestMatchers("/admin/**", "/sidebar", "/ad_contest").hasAuthority("ADMIN")
                        .requestMatchers("/contest/create", "/contest/submit").hasAnyAuthority("ADMIN", "COMPANY")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            Object principal = authentication.getPrincipal();
                            String redirectUrl = "/main";
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
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/main")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/login")
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
                .userDetailsService(combinedUserDetailsService)
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
        config.setAllowedOrigins(List.of("http://localhost:8888"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
