package com.example.bob.DTO;

import java.util.List;

/**
 * 이력서 전체 상세보기 DTO (조회 전용)
 */
public class ResumeDetailDTO {
    private String title;                              // 이력서 제목
    private List<String> jobTags;                      // 희망직무 태그
    private List<ResumeDetailSectionDTO> sections;     // 각 섹션 목록

    // 🔽 사용자 프로필 정보 추가
    private String userName;
    private String userNick;
    private String userEmail;
    private String userPhone;
    private String sex;
    private String birthday;
    private String region;
    private String mainLanguage;
    private String profileImageUrl;



    // Getter / Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getJobTags() { return jobTags; }
    public void setJobTags(List<String> jobTags) { this.jobTags = jobTags; }

    public List<ResumeDetailSectionDTO> getSections() { return sections; }
    public void setSections(List<ResumeDetailSectionDTO> sections) { this.sections = sections; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserNick() { return userNick; }
    public void setUserNick(String userNick) { this.userNick = userNick; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getMainLanguage() { return mainLanguage; }
    public void setMainLanguage(String mainLanguage) { this.mainLanguage = mainLanguage; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }


    // ✅ 여기 아래에 추가하세요
    private int age;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
