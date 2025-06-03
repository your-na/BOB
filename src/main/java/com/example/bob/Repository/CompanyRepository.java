    package com.example.bob.Repository;

    import com.example.bob.Entity.CompanyEntity;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;

    @Repository
    public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
        // 회원가입
        Boolean existsByCoIdLogin(String coIdLogin);

        //로그인
        Optional<CompanyEntity> findByCoIdLogin(String coIdLogin);

        // 기업명으로 회사 찾기
        CompanyEntity findByCoNick(String coNick);

        // 기업 회원 수 조회 (모든 기업 회원 수를 세는 기본 count 메서드)
        long count();

    }


