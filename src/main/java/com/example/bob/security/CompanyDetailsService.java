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
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다."));

        return new CompanyDetailsImpl(company); // CompanyDetailsImpl은 아래에서 만들어요!
    }
}
