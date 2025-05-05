package com.example.bob.DTO;

import java.util.List;

public class ResumeDTO {
    private String title;
    private List<ResumeSectionDTO> sections;
    private List<String> jobTags;

    // Getter / Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ResumeSectionDTO> getSections() {
        return sections;
    }

    public void setSections(List<ResumeSectionDTO> sections) {
        this.sections = sections;
    }

    public List<String> getJobTags() {
        return jobTags;
    }

    public void setJobTags(List<String> jobTags) {
        this.jobTags = jobTags;
    }
}
