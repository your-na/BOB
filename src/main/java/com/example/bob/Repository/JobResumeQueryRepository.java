package com.example.bob.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public class JobResumeQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void deleteByJobPostId(Long jobPostId) {
        entityManager.createNativeQuery("DELETE FROM job_resume WHERE job_post_id = ?1")
                .setParameter(1, jobPostId)
                .executeUpdate();
    }
}
