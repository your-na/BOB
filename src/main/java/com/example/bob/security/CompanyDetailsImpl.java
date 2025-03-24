package com.example.bob.security;

import com.example.bob.Entity.CompanyEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

@Getter
public class CompanyDetailsImpl implements CustomUserDetails {
    private final CompanyEntity companyEntity;

    public CompanyDetailsImpl(CompanyEntity companyEntity) {
        this.companyEntity = companyEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_COMPANY"));
    }

    @Override
    public String getPassword() {
        return companyEntity.getPwd();
    }

    @Override
    public String getUsername() {
        return companyEntity.getCoIdLogin();
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
        return companyEntity.getCompanyId();
    }

    @Override
    public String getUserType() {
        return "company";
    }

    public CompanyEntity getCompanyEntity() {
        return companyEntity;
    }
}
