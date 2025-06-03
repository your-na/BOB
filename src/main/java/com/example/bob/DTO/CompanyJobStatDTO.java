package com.example.bob.DTO;

import java.util.List;

// 📊 기업 전체 채용 통계 DTO
public class CompanyJobStatDTO {

    private int totalJobCount;        // 전체 공고 수
    private int totalApplicants;      // 총 지원자 수
    private int totalAccepted;        // 합격자 수
    private int totalRejected;        // 불합격자 수
    private int totalCanceled;        // 지원 취소 수

    private List<JobPostSummaryDTO> jobSummaries;  // 공고별 요약 통계 리스트

    // 👉 기본 생성자 + getter/setter
    public CompanyJobStatDTO() {}

    public int getTotalJobCount() {
        return totalJobCount;
    }

    public void setTotalJobCount(int totalJobCount) {
        this.totalJobCount = totalJobCount;
    }

    public int getTotalApplicants() {
        return totalApplicants;
    }

    public void setTotalApplicants(int totalApplicants) {
        this.totalApplicants = totalApplicants;
    }

    public int getTotalAccepted() {
        return totalAccepted;
    }

    public void setTotalAccepted(int totalAccepted) {
        this.totalAccepted = totalAccepted;
    }

    public int getTotalRejected() {
        return totalRejected;
    }

    public void setTotalRejected(int totalRejected) {
        this.totalRejected = totalRejected;
    }

    public int getTotalCanceled() {
        return totalCanceled;
    }

    public void setTotalCanceled(int totalCanceled) {
        this.totalCanceled = totalCanceled;
    }

    public List<JobPostSummaryDTO> getJobSummaries() {
        return jobSummaries;
    }

    public void setJobSummaries(List<JobPostSummaryDTO> jobSummaries) {
        this.jobSummaries = jobSummaries;
    }
}
