package com.example.bob.Entity;

public enum JobStatus {
    WAITING("모집 전"),
    OPEN("모집 중"),
    CLOSED("모집 마감"),
    HIRED("고용 완료");

    private final String label;

    JobStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
