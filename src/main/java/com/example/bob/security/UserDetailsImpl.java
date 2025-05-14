package com.example.bob.security;

import  com.example.bob.Entity.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserDetailsImpl implements CustomUserDetails {
    private final UserEntity userEntity;

    public UserDetailsImpl(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(userEntity.getRole()));
    }

    @Override
    public String getPassword() {
        return userEntity.getPwd();
    }

    @Override
    public String getUsername() {
        return userEntity.getUserIdLogin();
    }

    public String getUserNick(){
        return userEntity.getUserNick();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }

    @Override
    public Long getId() {
        return userEntity.getUserId();
    }

    @Override
    public String getUserType() {
        return "user";
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public UserEntity getUser() {
        return this.userEntity;
    }

}
