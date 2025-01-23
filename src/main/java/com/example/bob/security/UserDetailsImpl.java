package com.example.bob.security;

import com.example.bob.Entity.UserEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {

    private UserEntity userEntity;

    public UserDetailsImpl(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 권한 설정
        return Collections.singletonList(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return userEntity.getPwd();
    }

    @Override
    public String getUsername() {
        System.out.println("UserIdLogin: " + userEntity.getUserIdLogin());
        return userEntity.getUserIdLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 설정
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 설정
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부 설정
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부 설정
    }

    // UserEntity 정보 반환
    public UserEntity getUserEntity() {
        return userEntity;
    }

    // UserEntity 정보 갱신 후 SecurityContext에 반영
    public void updateUserEntity(UserEntity newUserEntity) {
        this.userEntity = newUserEntity;
        // 업데이트된 UserEntity를 SecurityContext에 반영
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(this, null, getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        System.out.println("SecurityContext updated with: " + authentication.getPrincipal());
    }
}
