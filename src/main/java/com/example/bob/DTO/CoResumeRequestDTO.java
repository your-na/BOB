package com.example.bob.DTO;

import java.util.List;
import java.util.Date;

public class CoResumeRequestDTO {

    private String title;
    private List<CoResumeSectionRequestDTO> sections;
    private Date createdAt;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<CoResumeSectionRequestDTO> getSections() {
        return sections;
    }

    public void setSections(List<CoResumeSectionRequestDTO> sections) {
        this.sections = sections;
    }

    public Date getCreatedAt() {  // 작성일 getter 추가
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {  // 작성일 setter 추가
        this.createdAt = createdAt;
    }
}

