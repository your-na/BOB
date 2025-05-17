package com.example.bob.DTO;

import java.util.List;

public class ResumeDTO {
    private String title;
    private List<ResumeSectionDTO> sections;
    private List<String> jobTags;

    // 사용자 정보 필드 추가
    private String userName;
    private String userNick;
    private String mainLanguage;
    private String birthday;
    private String sex;
    private String phone;
    private String email;
    private String address; // region
    private String profileImageUrl;

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getMainLanguage() {
        return mainLanguage;
    }

    public void setMainLanguage(String mainLanguage) {
        this.mainLanguage = mainLanguage;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserPhone() {
        return phone;
    }
    public String getUserEmail() {
        return email;
    }
    public String getRegion() {
        return address;
    }

}
