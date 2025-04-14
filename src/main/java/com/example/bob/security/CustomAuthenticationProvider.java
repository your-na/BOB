package com.example.bob.security;

import com.example.bob.Repository.CompanyRepository;
import com.example.bob.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Lazy // 순환 의존성 해결을 위해 @Lazy 추가
    private final UserDetailsService userService; // 일반 사용자

    @Lazy // 순환 의존성 해결을 위해 @Lazy 추가
    private final CompanyDetailsService companyDetailsService; // 기업 사용자

    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        UserDetails user;
        try {
            // 먼저 일반 사용자로 시도
            user = userService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            try{
            // 일반 사용자 없으면 기업 사용자로 시도
            user = companyDetailsService.loadUserByUsername(username);
        }catch (UsernameNotFoundException ex){
            throw new UsernameNotFoundException("존재하지 않는 아이디입니다.");
        }
        }


        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        return new UsernamePasswordAuthenticationToken(user, rawPassword, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
