package com.example.bob.Service;

import com.example.bob.DTO.CoJobPostRequestDTO;
import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Entity.JobApplicationStatus;

import com.example.bob.Repository.CoJobPostRepository;
import com.example.bob.Repository.CoResumeRepository;
import com.example.bob.Repository.CompanyRepository;
import com.example.bob.Repository.JobApplicationRepository;


import com.example.bob.security.CustomUserDetails;
import com.example.bob.security.CompanyDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.bob.DTO.CoJobPostResponseDTO;
import com.example.bob.DTO.CoJobPostDetailDTO;
import com.example.bob.Entity.JobStatus;
import com.example.bob.DTO.ResumeTitleDto;
import com.example.bob.DTO.ApplicantDTO;
import com.example.bob.DTO.CompanyJobStatDTO;
import com.example.bob.DTO.JobPostSummaryDTO;




import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.Comparator;




@Service
public class CoJobPostService {

    @Autowired
    private CoJobPostRepository coJobPostRepository;

    @Autowired
    private CoResumeRepository coResumeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    // 구인글 등록
    public Long saveJobPost(CoJobPostRequestDTO dto) {

        CoJobPostEntity entity = new CoJobPostEntity();

        entity.setTitle(dto.getTitle());
        entity.setCompanyIntro(dto.getCompanyIntro());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setCompanyLink(dto.getCompanyLink());
        entity.setCareer(dto.getCareer());
        entity.setEducation(dto.getEducation());
        entity.setPreference(dto.getPreference());
        entity.setEmploymentTypes(String.join(",", dto.getEmploymentTypes()));
        entity.setSalary(dto.getSalary());
        entity.setTime(dto.getTime());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setSurew(dto.getSurew());
        entity.setApplyType(dto.getApplyType());


        // 모집 상태 설정
        LocalDate today = LocalDate.now();
        LocalDate startDate = LocalDate.parse(dto.getStartDate());

        if (today.isBefore(startDate)) {
            entity.setStatus(JobStatus.WAITING);
        } else {
            entity.setStatus(JobStatus.OPEN);
        }

        // 현재 로그인된 사용자의 기업 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CompanyDetailsImpl companyDetails = (CompanyDetailsImpl) userDetails;  // CustomUserDetails에서 CompanyDetailsImpl로 캐스팅
        String currentUsername = companyDetails.getUsername();  // CompanyDetailsImpl에서 username을 가져옴

        // 기업 정보 조회
        CompanyEntity company = companyRepository.findByCoIdLogin(currentUsername)
                .orElseThrow(() -> new RuntimeException("로그인된 기업을 찾을 수 없습니다."));

        // 구인글에 현재 기업 연결
        entity.setCompany(company);

        // 이력서들 연결
        List<CoResumeEntity> resumes = coResumeRepository.findAllById(dto.getResumeIds());
        entity.setResumes(resumes);

        // 구인글 저장
        CoJobPostEntity saved = coJobPostRepository.save(entity);
        return saved.getId();

    }


    // 구인글 목록 조회 (모집 중인 공고만 반환)
    public List<CoJobPostResponseDTO> getAllJobPosts() {
        LocalDate today = LocalDate.now();

        return coJobPostRepository.findAll().stream()
                .peek(post -> {
                    LocalDate start = LocalDate.parse(post.getStartDate());
                    LocalDate end = LocalDate.parse(post.getEndDate());

                    // ✅ 상태 자동 업데이트 로직 (순서 중요)
                    if (today.isAfter(end)) {
                        post.setStatus(JobStatus.CLOSED);
                    } else if (today.isBefore(start)) {
                        post.setStatus(JobStatus.WAITING);
                    } else {
                        post.setStatus(JobStatus.OPEN);
                    }

                    coJobPostRepository.save(post); // 변경 저장
                })
                .filter(post -> post.getStatus() == JobStatus.OPEN)
                .map(post -> {
                    String coNick = post.getCompany() != null ? post.getCompany().getCoNick() : "알 수 없음";

                    // ✅ 지원자 수 가져오기
                    int applicantCount = jobApplicationRepository.countByJobPost_Id(post.getId());

                    return new CoJobPostResponseDTO(
                            post.getId(),
                            post.getTitle(),
                            post.getPhone(),
                            post.getCareer(),
                            coNick,
                            post.getStartDate(),
                            post.getEndDate(),
                            post.getStatus(),
                            applicantCount // ✅ 전달
                    );
                })
                .collect(Collectors.toList());
    }




    // 특정 구인 공고 상세 정보 조회
    public CoJobPostEntity getJobPostDetail(Long id) {
        // ID로 공고 조회 (없으면 예외 발생)
        return coJobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID의 공고를 찾을 수 없습니다."));
    }

    // 공고 상세 정보 + 이력서 제목 리스트 반환
    public CoJobPostDetailDTO getJobPostWithResumeTitles(Long id) {
        CoJobPostEntity entity = coJobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고를 찾을 수 없습니다."));

        List<ResumeTitleDto> resumeTitles = entity.getResumes().stream()
                .map(resume -> new ResumeTitleDto(resume.getId(), resume.getTitle()))
                .collect(Collectors.toList());

        return new CoJobPostDetailDTO(
                entity.getTitle(),
                entity.getCompanyIntro(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCareer(),
                entity.getEducation(),
                entity.getEmploymentTypes(),
                entity.getSalary(),
                entity.getTime(),
                entity.getPreference(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getSurew(),
                entity.getApplyType(),
                resumeTitles
        );
    }

    // 로그인한 기업이 작성한 모든 공고 목록을 반환
    public List<CoJobPostResponseDTO> getMyJobPosts() {
        // 🔐 로그인한 사용자 정보에서 기업 계정 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CompanyDetailsImpl companyDetails = (CompanyDetailsImpl) userDetails;
        String currentUsername = companyDetails.getUsername();

        // 🔍 로그인된 기업 정보 DB에서 조회
        CompanyEntity company = companyRepository.findByCoIdLogin(currentUsername)
                .orElseThrow(() -> new RuntimeException("로그인된 기업 정보를 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();

        return coJobPostRepository.findByCompany_CompanyId(company.getCompanyId()).stream()
                .peek(post -> {
                    LocalDate start = LocalDate.parse(post.getStartDate());
                    LocalDate end = LocalDate.parse(post.getEndDate());

                    if (today.isAfter(end)) {
                        post.setStatus(JobStatus.CLOSED);
                    } else if (today.isBefore(start)) {
                        post.setStatus(JobStatus.WAITING);
                    } else {
                        post.setStatus(JobStatus.OPEN);
                    }

                    coJobPostRepository.save(post);
                })
                .map(post -> {
                    String coNick = post.getCompany() != null ? post.getCompany().getCoNick() : "알 수 없음";

                    // ✅ 지원자 수 계산
                    int applicantCount = jobApplicationRepository.countByJobPost_IdAndStatus(post.getId(), JobApplicationStatus.SUBMITTED);

                    return new CoJobPostResponseDTO(
                            post.getId(),
                            post.getTitle(),
                            post.getPhone(),
                            post.getCareer(),
                            coNick,
                            post.getStartDate(),
                            post.getEndDate(),
                            post.getStatus(),
                            applicantCount // ✅ 추가
                    );
                })
                .collect(Collectors.toList());
    }

    // 📊 로그인한 기업의 채용 통계 계산 (month: 1 ~ 12, 없으면 전체)
    public CompanyJobStatDTO getCompanyJobStatistics(Integer month) {
        // 🔐 로그인된 기업 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CompanyDetailsImpl companyDetails = (CompanyDetailsImpl) userDetails;
        String currentUsername = companyDetails.getUsername();

        CompanyEntity company = companyRepository.findByCoIdLogin(currentUsername)
                .orElseThrow(() -> new RuntimeException("기업 정보를 찾을 수 없습니다."));

        // 🔍 기업 공고 전체 조회
        List<CoJobPostEntity> jobPosts = coJobPostRepository.findByCompany_CompanyId(company.getCompanyId());

        // ✅ month가 있으면 해당 월의 공고만 필터링
        if (month != null) {
            jobPosts = jobPosts.stream()
                    .filter(post -> {
                        LocalDate postDate = LocalDate.parse(post.getStartDate());
                        return postDate.getMonthValue() == month;
                    })
                    .collect(Collectors.toList());
        }

        // 📊 통계값 초기화
        int totalApplicants = 0;
        int totalAccepted = 0;
        int totalRejected = 0;
        int totalCanceled = 0;

        List<JobPostSummaryDTO> jobSummaries = new ArrayList<>();

        // 🔄 공고별 통계 계산
        for (CoJobPostEntity post : jobPosts) {
            Long jobId = post.getId();

            int applicants = jobApplicationRepository.countDistinctApplicantsByJobPostId(jobId);
            int accepted = jobApplicationRepository.countByJobPost_IdAndStatus(jobId, JobApplicationStatus.ACCEPTED);
            int rejected = jobApplicationRepository.countByJobPost_IdAndStatus(jobId, JobApplicationStatus.REJECTED);
            int canceled = jobApplicationRepository.countByJobPost_IdAndStatus(jobId, JobApplicationStatus.CANCELED);

            totalApplicants += applicants;
            totalAccepted += accepted;
            totalRejected += rejected;
            totalCanceled += canceled;

            jobSummaries.add(new JobPostSummaryDTO(post.getId(), post.getTitle(), applicants, accepted));
        }

        CompanyJobStatDTO dto = new CompanyJobStatDTO();
        dto.setTotalJobCount(jobPosts.size());
        dto.setTotalApplicants(totalApplicants);
        dto.setTotalAccepted(totalAccepted);
        dto.setTotalRejected(totalRejected);
        dto.setTotalCanceled(totalCanceled);
        dto.setJobSummaries(jobSummaries);

        return dto;
    }

    // 🔁 전체 조회용 기본 메서드도 유지
    public CompanyJobStatDTO getCompanyJobStatistics() {
        return getCompanyJobStatistics(null); // 👉 null 넘겨서 전체 월 기준으로 호출
    }

    public List<String> getAvailableJobPostMonths() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CompanyDetailsImpl companyDetails = (CompanyDetailsImpl) userDetails;
        String currentUsername = companyDetails.getUsername();

        CompanyEntity company = companyRepository.findByCoIdLogin(currentUsername)
                .orElseThrow(() -> new RuntimeException("기업 정보를 찾을 수 없습니다."));

        return coJobPostRepository.findByCompany_CompanyId(company.getCompanyId()).stream()
                .map(post -> {
                    LocalDate date = LocalDate.parse(post.getStartDate());
                    return date.getYear() + "-" + String.format("%02d", date.getMonthValue()); // 예: "2025-06"
                })
                .distinct()
                .sorted(Comparator.reverseOrder()) // 최신순 정렬
                .collect(Collectors.toList());
    }

    // ✅ 공고 삭제 메서드
    public void deleteJobPost(Long id) {
        // 1. 삭제할 공고를 ID로 조회
        CoJobPostEntity post = coJobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("삭제할 공고를 찾을 수 없습니다."));

        // 2. 삭제 실행
        coJobPostRepository.delete(post);
    }







}

