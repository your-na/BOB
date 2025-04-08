package com.example.bob.DTO;

import java.util.List;

public class CoResumeRequestDTO {

    private String title;
    private List<CoResumeSectionRequestDTO> sections;

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
}

