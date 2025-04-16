    package com.example.bob.Repository;

    import com.example.bob.Entity.CoResumeSectionEntity;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    @Repository
    public interface CoResumeSectionRepository extends JpaRepository<CoResumeSectionEntity, Long> {
        // 추가적인 커스텀 쿼리 메서드가 필요하면 여기에 정의할 수 있습니다.
    }
