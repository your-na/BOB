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
    private final UserEntity userEntity;

    public UserDetailsImpl(UserEntity userEntity) {
        this.userEntity = userEntity;
        System.out.println("🔍 UserDetailsImpl 생성: userId=" + (userEntity != null ? userEntity.getUserId() : "NULL"));
    }

    public UserEntity getUserEntity() {
        System.out.println("🔍 getUserEntity() 호출: userId=" + (userEntity != null ? userEntity.getUserId() : "NULL"));
        return userEntity;
    }

    @Override
    public String getUsername() {
        return userEntity.getUserIdLogin();
    }

    @Override
    public String getPassword() {
        return userEntity.getPwd();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
