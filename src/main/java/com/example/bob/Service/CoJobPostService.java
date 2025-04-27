package com.example.bob.Service;

import com.example.bob.DTO.CoJobPostRequestDTO;
import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Entity.CompanyEntity;
import com.example.bob.Repository.CoJobPostRepository;
import com.example.bob.Repository.CoResumeRepository;
import com.example.bob.Repository.CompanyRepository;
import com.example.bob.security.CustomUserDetails;
import com.example.bob.security.CompanyDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.bob.DTO.CoJobPostResponseDTO;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class CoJobPostService {

    @Autowired
    private CoJobPostRepository coJobPostRepository;

    @Autowired
    private CoResumeRepository coResumeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    // 구인글 등록
    public void saveJobPost(CoJobPostRequestDTO dto) {
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
        coJobPostRepository.save(entity);
    }

    // 구인글 목록 조회
    public List<CoJobPostResponseDTO> getAllJobPosts() {
        return coJobPostRepository.findAll().stream().map(post -> {
            String coNick = post.getCompany() != null ? post.getCompany().getCoNick() : "알 수 없음";
            return new CoJobPostResponseDTO(
                    post.getTitle(),
                    post.getPhone(),
                    post.getCareer(),
                    coNick
            );
        }).collect(Collectors.toList());
    }
}

