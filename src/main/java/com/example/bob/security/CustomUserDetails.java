package com.example.bob.security;

import com.example.bob.Entity.UserEntity;
import org.springframework.security.core.userdetails.UserDetails;

public interface CustomUserDetails extends UserDetails {
    Long getId();
    String getUserType(); // "user" 또는 "company"

    Object getUserEntity();
}