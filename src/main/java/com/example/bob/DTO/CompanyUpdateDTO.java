package com.example.bob.DTO;

public class CompanyUpdateDTO {
    // 수정 가능 항목 추가 (이름은 불가능하지 않나?)
    private String coEmail;
    private String coPhone;
    private String coBio;
    private String coImageUrl;

    public String getCoEmail() { return coEmail; }
    public void setCoEmail(String coEmail) { this.coEmail = coEmail; }

    public String getCoPhone() { return coPhone; }
    public void setCoPhone(String coPhone) { this.coPhone = coPhone; }

    public String getCoBio() { return coBio; }
    public void setCoBio(String coBio) { this.coBio = coBio; }

    public String getCoImageUrl() { return coImageUrl; }
    public void setCoImageUrl(String coImageUrl) { this.coImageUrl = coImageUrl; }

    @Override
    public String toString() {
        return "CompanyUpdateDTO{" +
                "coEmail='" + coEmail + '\'' +
                ", coPhone='" + coPhone + '\'' +
                ", coBio='" + coBio + '\'' +
                ", coImageUrl='" + coImageUrl + '\'' +
                '}';
    }
}
