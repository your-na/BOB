package com.example.bob.DTO;

public class UserUpdateDTO {
    private String userNick;
    private String userEmail;
    private String userBio;
    private String mainLanguage;
    private String profileImageUrl;
    private String region;
    private String nameHanja;
    private String nameEng;



    // Getters and Setters
    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserBio() {
        return userBio;
    }

    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }

    public String getMainLanguage() {
        return mainLanguage;
    }

    public void setMainLanguage(String mainLanguage) {
        this.mainLanguage = mainLanguage;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getNameHanja() {
        return nameHanja;
    }

    public void setNameHanja(String nameHanja) {
        this.nameHanja = nameHanja;
    }

    public String getNameEng() {
        return nameEng;
    }

    public void setNameEng(String nameEng) {
        this.nameEng = nameEng;
    }



    @Override
    public String toString() {
        return "UserUpdateDTO{" +
                "userNick='" + userNick + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userBio='" + userBio + '\'' +
                ", mainLanguage='" + mainLanguage + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                '}';
    }
}
