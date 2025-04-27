package com.example.bob.Service;

import com.example.bob.DTO.CoJobPostRequestDTO;
import com.example.bob.Entity.CoJobPostEntity;
import com.example.bob.Entity.CoResumeEntity;
import com.example.bob.Repository.CoJobPostRepository;
import com.example.bob.Repository.CoResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoJobPostService {

    @Autowired
    private CoJobPostRepository coJobPostRepository;

    @Autowired
    private CoResumeRepository coResumeRepository;

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

        List<CoResumeEntity> resumes = coResumeRepository.findAllById(dto.getResumeIds());
        entity.setResumes(resumes);

        coJobPostRepository.save(entity);
    }
}
