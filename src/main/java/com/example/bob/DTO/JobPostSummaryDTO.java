package com.example.bob.DTO;

// 📌 개별 공고의 간단한 요약 통계 DTO
public class JobPostSummaryDTO {

    private Long jobId;          // 공고 ID
    private String title;        // 공고 제목
    private int applicantCount;  // 지원자 수
    private int acceptedCount;   // 채용(합격) 수

    public JobPostSummaryDTO() {}

    public JobPostSummaryDTO(Long jobId, String title, int applicantCount, int acceptedCount) {
        this.jobId = jobId;
        this.title = title;
        this.applicantCount = applicantCount;
        this.acceptedCount = acceptedCount;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getApplicantCount() {
        return applicantCount;
    }

    public void setApplicantCount(int applicantCount) {
        this.applicantCount = applicantCount;
    }

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount = acceptedCount;
    }
}
