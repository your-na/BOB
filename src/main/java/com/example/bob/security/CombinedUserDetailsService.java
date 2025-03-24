package com.example.bob.security;

import com.example.bob.Repository.UserRepository;
import com.example.bob.Repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CombinedUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUserIdLogin(username)
                .map(UserDetailsImpl::new)
                .map(user -> (UserDetails) user)
                .orElseGet(() ->
                        companyRepository.findByCoIdLogin(username)
                                .map(CompanyDetailsImpl::new)
                                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username))
                );
    }
}