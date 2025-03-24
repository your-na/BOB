package com.example.bob.security;

import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyDetailsService implements UserDetailsService {

    private final CompanyRepository companyRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        CompanyEntity company = companyRepository.findByCoIdLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("기업 아이디를 찾을 수 없습니다: " + username));

        return new CompanyDetailsImpl(company); // CompanyDetailsImpl은 아래에서 만들어요!
    }
}
