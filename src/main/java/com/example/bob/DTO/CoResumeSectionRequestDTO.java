package com.example.bob.DTO;

import java.util.List;

public class CoResumeSectionRequestDTO {

    private String type;
    private String title;
    private String comment;
    private String content;  // 서술형일 때 사용
    private List<String> tags;  // 선택형일 때 사용

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

